package com.example.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface SupabaseService {

    @POST("auth/v1/signup")
    suspend fun signUp(
        @Header("apikey") apiKey: String,
        @Body request: AuthRequest
    ): Response<AuthResponse>

    @POST("auth/v1/token?grant_type=password")
    suspend fun signIn(
        @Header("apikey") apiKey: String,
        @Body request: AuthRequest
    ): Response<AuthResponse>

    @GET("auth/v1/user")
    suspend fun getUser(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String
    ): Response<SupabaseUser>

    @GET("rest/v1/debts")
    suspend fun getDebts(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Query("select") select: String = "*"
    ): Response<List<SupabaseDebtDto>>

    @POST("rest/v1/debts")
    suspend fun insertDebt(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Header("Prefer") prefer: String = "return=representation",
        @Body debt: SupabaseDebtDto
    ): Response<List<SupabaseDebtDto>>

    @PATCH("rest/v1/debts")
    suspend fun updateDebt(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Query("id") idFilter: String,
        @Body debt: SupabaseDebtDto
    ): Response<Unit>

    @DELETE("rest/v1/debts")
    suspend fun deleteDebt(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Query("id") idFilter: String
    ): Response<Unit>

    @GET("rest/v1/payments")
    suspend fun getPayments(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Query("select") select: String = "*"
    ): Response<List<SupabasePaymentDto>>

    @POST("rest/v1/payments")
    suspend fun insertPayment(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Header("Prefer") prefer: String = "return=representation",
        @Body payment: SupabasePaymentDto
    ): Response<List<SupabasePaymentDto>>
}
