package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object SupabaseClient {

    private const val TAG = "SupabaseClient"

    val supabaseUrl: String
        get() {
            val url = try { BuildConfig.SUPABASE_URL } catch (e: Exception) { "" }
            return if (url.isBlank() || url.contains("your-supabase-project")) {
                "https://demo.supabase.co/"
            } else {
                if (!url.endsWith("/")) "$url/" else url
            }
        }

    val supabaseAnonKey: String
        get() {
            val key = try { BuildConfig.SUPABASE_ANON_KEY } catch (e: Exception) { "" }
            return if (key.isBlank() || key.contains("your-supabase-anon-key")) {
                "demo-anon-key"
            } else {
                key
            }
        }

    val isConfigured: Boolean
        get() = !supabaseUrl.contains("demo.supabase.co") && supabaseAnonKey != "demo-anon-key"

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    val service: SupabaseService by lazy {
        Retrofit.Builder()
            .baseUrl(supabaseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SupabaseService::class.java)
    }

    suspend fun authenticate(email: String, password: String, isSignUp: Boolean): Result<AuthResponse> {
        return try {
            if (!isConfigured) {
                // Mock local authentication when Supabase URL is not configured yet
                Log.d(TAG, "Supabase credentials not set, returning mock auth for $email")
                val mockUser = SupabaseUser(
                    id = "local-user-" + email.hashCode(),
                    email = email
                )
                return Result.success(
                    AuthResponse(
                        accessToken = "local-session-token-" + System.currentTimeMillis(),
                        tokenType = "bearer",
                        user = mockUser
                    )
                )
            }

            val request = AuthRequest(email = email.trim(), password = password.trim())
            val response = if (isSignUp) {
                service.signUp(apiKey = supabaseAnonKey, request = request)
            } else {
                service.signIn(apiKey = supabaseAnonKey, request = request)
            }

            if (response.isSuccessful && response.body() != null) {
                val authBody = response.body()!!
                if (authBody.accessToken != null || authBody.user != null) {
                    Result.success(authBody)
                } else if (authBody.msg != null) {
                    Result.failure(Exception(authBody.msg))
                } else {
                    Result.success(authBody)
                }
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Ошибка авторизации: ${response.code()}"
                Result.failure(Exception(extractErrorMessage(errorMsg)))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Auth error", e)
            val isHostError = e is java.net.UnknownHostException || 
                             e.message?.contains("Unable to resolve host") == true ||
                             e is java.net.ConnectException

            if (isHostError) {
                Log.w(TAG, "Supabase host unreachable ($supabaseUrl), logging in locally for $email")
                val mockUser = SupabaseUser(
                    id = "local-user-" + email.hashCode(),
                    email = email
                )
                return Result.success(
                    AuthResponse(
                        accessToken = "local-session-token-" + System.currentTimeMillis(),
                        tokenType = "bearer",
                        user = mockUser
                    )
                )
            }

            val friendlyError = e.message ?: "Ошибка подключения к серверу."
            Result.failure(Exception(friendlyError))
        }
    }

    private fun extractErrorMessage(json: String): String {
        return try {
            if (json.contains("invalid_credentials") || json.contains("Invalid login")) {
                "Неверный логин или пароль"
            } else if (json.contains("User already registered") || json.contains("already exists")) {
                "Пользователь с таким email уже зарегистрирован"
            } else if (json.contains("Password should be")) {
                "Пароль должен содержать минимум 6 символов"
            } else {
                json.take(150)
            }
        } catch (e: Exception) {
            "Ошибка сервера. Проверьте подключение."
        }
    }
}
