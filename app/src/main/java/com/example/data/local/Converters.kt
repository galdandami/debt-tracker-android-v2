package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.DebtType

class Converters {
    @TypeConverter
    fun fromDebtType(type: DebtType): String = type.name

    @TypeConverter
    fun toDebtType(value: String): DebtType = runCatching { DebtType.valueOf(value) }.getOrDefault(DebtType.OWED_TO_ME)
}
