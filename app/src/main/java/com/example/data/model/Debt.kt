package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "debts")
data class Debt(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val personName: String,
    val type: DebtType,
    val initialAmount: Double,
    val currentAmount: Double,
    val currency: String = "₽",
    val createdAt: Long = System.currentTimeMillis(),
    val dueDate: Long? = null,
    val comment: String = "",
    val isClosed: Boolean = false,
    val closedAt: Long? = null,
    val isForgiven: Boolean = false
)
