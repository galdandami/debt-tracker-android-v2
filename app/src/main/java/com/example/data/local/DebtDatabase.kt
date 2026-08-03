package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.Debt
import com.example.data.model.PaymentHistory

@Database(
    entities = [Debt::class, PaymentHistory::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class DebtDatabase : RoomDatabase() {
    abstract fun debtDao(): DebtDao

    companion object {
        @Volatile
        private var INSTANCE: DebtDatabase? = null

        fun getDatabase(context: Context): DebtDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DebtDatabase::class.java,
                    "debt_tracker_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
