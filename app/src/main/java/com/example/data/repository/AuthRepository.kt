package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.remote.SupabaseClient
import com.example.data.remote.SupabaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserSession(
    val id: String,
    val email: String,
    val token: String,
    val isLoggedIn: Boolean = true
)

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class LoggedIn(val session: UserSession) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentSession = MutableStateFlow<UserSession?>(null)
    val currentSession: StateFlow<UserSession?> = _currentSession.asStateFlow()

    init {
        restoreSession()
    }

    private fun restoreSession() {
        val token = prefs.getString("access_token", null)
        val email = prefs.getString("user_email", null)
        val id = prefs.getString("user_id", null)

        if (!token.isNullOrBlank() && !email.isNullOrBlank() && !id.isNullOrBlank()) {
            val session = UserSession(id = id!!, email = email!!, token = token!!)
            _currentSession.value = session
            _authState.value = AuthState.LoggedIn(session)
        } else {
            _authState.value = AuthState.Idle
        }
    }

    private fun String?.isNull_or_empty(): Boolean = this == null || this.trim().isEmpty()

    suspend fun login(email: String, password: String): Result<UserSession> {
        _authState.value = AuthState.Loading
        val result = SupabaseClient.authenticate(email = email, password = password, isSignUp = false)

        return if (result.isSuccess) {
            val response = result.getOrNull()
            val token = response?.accessToken ?: "token_${System.currentTimeMillis()}"
            val userId = response?.user?.id ?: "user_${email.hashCode()}"
            val userEmail = response?.user?.email ?: email

            val session = UserSession(id = userId, email = userEmail, token = token)
            saveSession(session)
            _currentSession.value = session
            _authState.value = AuthState.LoggedIn(session)
            Result.success(session)
        } else {
            val errorMsg = result.exceptionOrNull()?.message ?: "Не удалось войти"
            _authState.value = AuthState.Error(errorMsg)
            Result.failure(Exception(errorMsg))
        }
    }

    suspend fun register(email: String, password: String): Result<UserSession> {
        _authState.value = AuthState.Loading
        val result = SupabaseClient.authenticate(email = email, password = password, isSignUp = true)

        return if (result.isSuccess) {
            val response = result.getOrNull()
            val token = response?.accessToken ?: "token_${System.currentTimeMillis()}"
            val userId = response?.user?.id ?: "user_${email.hashCode()}"
            val userEmail = response?.user?.email ?: email

            val session = UserSession(id = userId, email = userEmail, token = token)
            saveSession(session)
            _currentSession.value = session
            _authState.value = AuthState.LoggedIn(session)
            Result.success(session)
        } else {
            val errorMsg = result.exceptionOrNull()?.message ?: "Не удалось зарегистрироваться"
            _authState.value = AuthState.Error(errorMsg)
            Result.failure(Exception(errorMsg))
        }
    }

    fun continueAsGuest() {
        val guestSession = UserSession(
            id = "guest_user",
            email = "demo@guest.local",
            token = "guest_token"
        )
        _currentSession.value = guestSession
        _authState.value = AuthState.LoggedIn(guestSession)
    }

    fun logout() {
        prefs.edit().clear().apply()
        _currentSession.value = null
        _authState.value = AuthState.Idle
    }

    private fun saveSession(session: UserSession) {
        prefs.edit()
            .putString("access_token", session.token)
            .putString("user_email", session.email)
            .putString("user_id", session.id)
            .apply()
    }
}
