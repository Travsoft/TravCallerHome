package com.cartravelsdailerapp.Repositorys.DAO

import androidx.room.*
import com.cartravelsdailerapp.models.CallHistory
import com.cartravelsdailerapp.models.Contact

@Dao
interface CallHistoryDao {
    @Query("SELECT * FROM CallHistory group by number ORDER BY id DESC")
    fun getAllCallLogs(): List<CallHistory>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(listofCallHistory: List<CallHistory>)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllContacts(listofContacts: List<Contact>)
    @Insert
    fun insertCallHistory(callHistory: CallHistory)

    @Delete
    fun delete(callHistory: CallHistory)

    @Query("SELECT * FROM CallHistory WHERE number = :number")
    fun callDataByNumber(number: String): List<CallHistory>

    @Query("SELECT * FROM CallHistory WHERE number || name LIKE '%' || :searchQuery || '%'")
    fun searchCall(searchQuery: String): List<CallHistory>

    @Query("SELECT * FROM Contact WHERE number || name LIKE '%' || :searchQuery || '%'")
    fun searchContactCall(searchQuery: String): List<Contact>

    @Query("UPDATE Contact SET isFavourites =:isFavourites WHERE id =:id")
    fun updateContacts(isFavourites: Boolean, id: Int)

    @Query("SELECT * FROM Contact WHERE isFavourites =:isFavourites")
    fun getAllFavouriteContacts(isFavourites: Boolean): List<Contact>

    @Query("SELECT * FROM Contact WHERE number =:number")
    fun getFavouriteContactsByNumber(number: String): Contact
}