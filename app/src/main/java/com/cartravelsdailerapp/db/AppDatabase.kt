package com.cartravelsdailerapp.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cartravelsdailerapp.Repositorys.DAO.CallHistoryDao
import com.cartravelsdailerapp.models.CallHistory

@Database(entities = [CallHistory::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun CallHistoryDao(): CallHistoryDao
}