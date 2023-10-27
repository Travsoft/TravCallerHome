package com.cartravelsdailerapp.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cartravelsdailerapp.Repositorys.DAO.CallHistoryDao
import com.cartravelsdailerapp.models.CallHistory
import com.cartravelsdailerapp.models.Contact

@Database(entities = [CallHistory::class,Contact::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun CallHistoryDao(): CallHistoryDao
}