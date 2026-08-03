package com.example.data.repository

import android.util.Log
import com.example.data.local.DebtDao
import com.example.data.model.Debt
import com.example.data.model.DebtType
import com.example.data.remote.SupabaseClient
import com.example.data.remote.SupabaseDebtDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SupabaseSyncManager(
    private val debtDao: DebtDao
) {
    private val TAG = "SupabaseSyncManager"

    suspend fun syncWithSupabase(accessToken: String): Result<Int> = withContext(Dispatchers.IO) {
        if (!SupabaseClient.isConfigured) {
            Log.d(TAG, "Supabase credentials not configured in .env; sync skipped.")
            return@withContext Result.success(0)
        }

        try {
            val response = SupabaseClient.service.getDebts(
                apiKey = SupabaseClient.supabaseAnonKey,
                bearerToken = "Bearer $accessToken"
            )

            if (response.isSuccessful && response.body() != null) {
                val dtos = response.body()!!
                var syncedCount = 0
                for (dto in dtos) {
                    val localDebt = Debt(
                        id = dto.id ?: 0,
                        personName = dto.personName,
                        type = if (dto.type == "I_OWE") DebtType.I_OWE else DebtType.OWED_TO_ME,
                        initialAmount = dto.initialAmount,
                        currentAmount = dto.currentAmount,
                        currency = dto.currency,
                        dueDate = dto.dueDate,
                        comment = dto.comment,
                        isClosed = dto.isClosed,
                        closedAt = dto.closedAt,
                        isForgiven = dto.isForgiven
                    )
                    debtDao.insertDebt(localDebt)
                    syncedCount++
                }
                Result.success(syncedCount)
            } else {
                Result.failure(Exception("Supabase HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sync error", e)
            Result.failure(e)
        }
    }

    suspend fun pushDebtToSupabase(debt: Debt, accessToken: String) = withContext(Dispatchers.IO) {
        if (!SupabaseClient.isConfigured || accessToken.isEmpty()) return@withContext

        try {
            val dto = SupabaseDebtDto(
                id = if (debt.id > 0) debt.id else null,
                personName = debt.personName,
                type = debt.type.name,
                initialAmount = debt.initialAmount,
                currentAmount = debt.currentAmount,
                currency = debt.currency,
                dueDate = debt.dueDate,
                comment = debt.comment,
                isClosed = debt.isClosed,
                closedAt = debt.closedAt,
                isForgiven = debt.isForgiven
            )
            SupabaseClient.service.insertDebt(
                apiKey = SupabaseClient.supabaseAnonKey,
                bearerToken = "Bearer $accessToken",
                debt = dto
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error pushing debt to Supabase", e)
        }
    }
}
