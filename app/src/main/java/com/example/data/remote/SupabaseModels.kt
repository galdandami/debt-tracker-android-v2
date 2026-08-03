package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class AuthRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = false)
data class AuthResponse(
    @Json(name = "access_token") val accessToken: String? = null,
    @Json(name = "token_type") val tokenType: String? = null,
    @Json(name = "user") val user: SupabaseUser? = null,
    @Json(name = "error_description") val errorDescription: String? = null,
    @Json(name = "msg") val msg: String? = null
)

@JsonClass(generateAdapter = false)
data class SupabaseUser(
    @Json(name = "id") val id: String,
    @Json(name = "email") val email: String? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = false)
data class SupabaseDebtDto(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "user_id") val userId: String? = null,
    @Json(name = "person_name") val personName: String,
    @Json(name = "type") val type: String,
    @Json(name = "initial_amount") val initialAmount: Double,
    @Json(name = "current_amount") val currentAmount: Double,
    @Json(name = "currency") val currency: String = "₽",
    @Json(name = "due_date") val dueDate: Long? = null,
    @Json(name = "comment") val comment: String = "",
    @Json(name = "is_closed") val isClosed: Boolean = false,
    @Json(name = "closed_at") val closedAt: Long? = null,
    @Json(name = "is_forgiven") val isForgiven: Boolean = false
)

@JsonClass(generateAdapter = false)
data class SupabasePaymentDto(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "debt_id") val debtId: Long,
    @Json(name = "amount") val amount: Double,
    @Json(name = "payment_date") val paymentDate: Long,
    @Json(name = "note") val note: String = ""
)
