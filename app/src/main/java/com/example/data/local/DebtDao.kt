package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Debt
import com.example.data.model.DebtType
import com.example.data.model.PaymentHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtDao {
    @Query("SELECT * FROM debts WHERE isClosed = 0 ORDER BY createdAt DESC")
    fun getAllActiveDebtsFlow(): Flow<List<Debt>>

    @Query("SELECT * FROM debts WHERE isClosed = 0 AND type = :type ORDER BY createdAt DESC")
    fun getActiveDebtsByTypeFlow(type: DebtType): Flow<List<Debt>>

    @Query("SELECT * FROM debts WHERE isClosed = 1 ORDER BY closedAt DESC")
    fun getClosedDebtsFlow(): Flow<List<Debt>>

    @Query("SELECT * FROM debts WHERE id = :id LIMIT 1")
    fun getDebtByIdFlow(id: Long): Flow<Debt?>

    @Query("SELECT * FROM debts WHERE id = :id LIMIT 1")
    suspend fun getDebtById(id: Long): Debt?

    @Query("SELECT DISTINCT personName FROM debts ORDER BY personName ASC")
    fun getDistinctPersonNamesFlow(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: Debt): Long

    @Update
    suspend fun updateDebt(debt: Debt)

    @Delete
    suspend fun deleteDebt(debt: Debt)

    @Query("SELECT * FROM payment_history WHERE debtId = :debtId ORDER BY paymentDate DESC")
    fun getPaymentsForDebtFlow(debtId: Long): Flow<List<PaymentHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentHistory): Long

    @Query("DELETE FROM payment_history WHERE debtId = :debtId")
    suspend fun deletePaymentsForDebt(debtId: Long)

    @Query("DELETE FROM debts")
    suspend fun deleteAllDebts()

    @Query("DELETE FROM payment_history")
    suspend fun deleteAllPayments()
}
