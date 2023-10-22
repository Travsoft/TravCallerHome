package com.cartravelsdailerapp.db

import android.content.Context
import androidx.room.Room
import com.cartravelsdailerapp.PrefUtils

object DatabaseBuilder {
    private var INSTANCE: AppDatabase? = null
    fun getInstance(context: Context): AppDatabase {
        if (INSTANCE == null) {
            synchronized(AppDatabase::class) {
                INSTANCE = buildRoomDB(context)
            }
        }
        return INSTANCE!!
    }

    private fun buildRoomDB(context: Context) =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            PrefUtils.PackageName
        ).allowMainThreadQueries().build()
}