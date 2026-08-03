package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payment_history",
    foreignKeys = [
        ForeignKey(
            entity = Debt::class,
            parentColumns = ["id"],
            childColumns = ["debtId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["debtId"])]
)
data class PaymentHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val debtId: Long,
    val amount: Double,
    val paymentDate: Long = System.currentTimeMillis(),
    val note: String = ""
)
