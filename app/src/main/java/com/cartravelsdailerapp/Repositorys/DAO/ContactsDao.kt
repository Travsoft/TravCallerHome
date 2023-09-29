package com.cartravelsdailerapp.Repositorys.DAO

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.cartravelsdailerapp.models.CallHistory

@Dao
interface CallHistoryDao {

    @Query("SELECT * FROM CallHistory")
    fun getAll(): List<CallHistory>

    @Insert
    fun insertAll(Courses: List<CallHistory>)

    @Delete
    fun delete(Course: CallHistory)

}