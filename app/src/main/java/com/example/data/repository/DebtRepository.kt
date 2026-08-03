package com.example.data.repository

import com.example.data.local.DebtDao
import com.example.data.model.Debt
import com.example.data.model.DebtType
import com.example.data.model.PaymentHistory
import kotlinx.coroutines.flow.Flow
import kotlin.math.max

class DebtRepository(private val debtDao: DebtDao) {

    val allActiveDebts: Flow<List<Debt>> = debtDao.getAllActiveDebtsFlow()
    val closedDebts: Flow<List<Debt>> = debtDao.getClosedDebtsFlow()
    val distinctPersonNames: Flow<List<String>> = debtDao.getDistinctPersonNamesFlow()

    fun getActiveDebtsByType(type: DebtType): Flow<List<Debt>> {
        return debtDao.getActiveDebtsByTypeFlow(type)
    }

    fun getDebtByIdFlow(id: Long): Flow<Debt?> {
        return debtDao.getDebtByIdFlow(id)
    }

    fun getPaymentsForDebtFlow(debtId: Long): Flow<List<PaymentHistory>> {
        return debtDao.getPaymentsForDebtFlow(debtId)
    }

    suspend fun createDebt(
        personName: String,
        type: DebtType,
        amount: Double,
        currency: String = "₽",
        dueDate: Long? = null,
        comment: String = ""
    ): Long {
        val debt = Debt(
            personName = personName.trim(),
            type = type,
            initialAmount = amount,
            currentAmount = amount,
            currency = currency,
            createdAt = System.currentTimeMillis(),
            dueDate = dueDate,
            comment = comment.trim(),
            isClosed = false
        )
        return debtDao.insertDebt(debt)
    }

    suspend fun updateDebt(debt: Debt) {
        debtDao.updateDebt(debt)
    }

    suspend fun addPartialPayment(debtId: Long, paymentAmount: Double, note: String = ""): Boolean {
        val debt = debtDao.getDebtById(debtId) ?: return false
        val newRemaining = max(0.0, debt.currentAmount - paymentAmount)
        val isNowClosed = newRemaining <= 0.001

        val updatedDebt = debt.copy(
            currentAmount = newRemaining,
            isClosed = isNowClosed,
            closedAt = if (isNowClosed) System.currentTimeMillis() else debt.closedAt
        )

        debtDao.updateDebt(updatedDebt)
        debtDao.insertPayment(
            PaymentHistory(
                debtId = debtId,
                amount = paymentAmount,
                paymentDate = System.currentTimeMillis(),
                note = note.trim()
            )
        )
        return true
    }

    suspend fun settleInFull(debtId: Long, note: String = "Погашено полностью"): Boolean {
        val debt = debtDao.getDebtById(debtId) ?: return false
        val remainingAmount = debt.currentAmount

        val updatedDebt = debt.copy(
            currentAmount = 0.0,
            isClosed = true,
            closedAt = System.currentTimeMillis(),
            isForgiven = false
        )

        debtDao.updateDebt(updatedDebt)
        if (remainingAmount > 0) {
            debtDao.insertPayment(
                PaymentHistory(
                    debtId = debtId,
                    amount = remainingAmount,
                    paymentDate = System.currentTimeMillis(),
                    note = note
                )
            )
        }
        return true
    }

    suspend fun forgiveDebt(debtId: Long): Boolean {
        val debt = debtDao.getDebtById(debtId) ?: return false
        val updatedDebt = debt.copy(
            isClosed = true,
            closedAt = System.currentTimeMillis(),
            isForgiven = true
        )
        debtDao.updateDebt(updatedDebt)
        return true
    }

    suspend fun reopenDebt(debtId: Long): Boolean {
        val debt = debtDao.getDebtById(debtId) ?: return false
        val updatedDebt = debt.copy(
            isClosed = false,
            closedAt = null,
            isForgiven = false
        )
        debtDao.updateDebt(updatedDebt)
        return true
    }

    suspend fun deleteDebt(debt: Debt) {
        debtDao.deleteDebt(debt)
    }
}
