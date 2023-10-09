package com.cartravelsdailerapp.Repositorys.DAO

import androidx.room.*
import com.cartravelsdailerapp.models.CallHistory

@Dao
interface CallHistoryDao {

    @Query("SELECT * FROM CallHistory ORDER BY id ASC")
    fun getAll(): List<CallHistory>

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(listofCallHistory: List<CallHistory>)
    @Insert
    fun insertCallHistory(callHistory: CallHistory)

    @Delete
    fun delete(callHistory: CallHistory)

}