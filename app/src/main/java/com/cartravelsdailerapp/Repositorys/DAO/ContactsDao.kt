package com.cartravelsdailerapp.Repositorys.DAO

import androidx.room.*
import com.cartravelsdailerapp.models.AutoCallContacts
import com.cartravelsdailerapp.models.CallHistory
import com.cartravelsdailerapp.models.Contact
import com.cartravelsdailerapp.models.FavouritesContacts

@Dao
interface CallHistoryDao {
    // @Query("SELECT * FROM CallHistory group by number ORDER BY id DESC")
    @Query("SELECT * FROM CallHistory group by date ORDER BY id DESC")
    fun getAllCallLogs(): List<CallHistory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(listofCallHistory: List<CallHistory>)

    @Insert
    fun insertCallHistory(callHistory: CallHistory)

    @Delete
    fun delete(callHistory: CallHistory)

    @Query("SELECT * FROM CallHistory WHERE number = :number")
    fun callDataByNumber(number: String): List<CallHistory>

    @Query("SELECT * FROM CallHistory WHERE name || number LIKE :searchQuery || '%'")
    fun searchCall(searchQuery: String): List<CallHistory>

    @Query("SELECT * FROM Contact WHERE number || name LIKE '%' || :searchQuery || '%'")
    fun searchContactCall(searchQuery: String): List<Contact>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFavouriteContact(fcontact: Contact)

    @Query("UPDATE Contact SET isFavourites=:isFavourites WHERE id = :id")
    fun updateFavouriteContact(isFavourites: Boolean, id: Int)

    @Query("SELECT * FROM Contact WHERE isFavourites = :isFavourites group by number ORDER BY name DESC")
    fun getAllFavouriteContacts(isFavourites: Boolean): List<Contact>

    @Query("SELECT * FROM Contact WHERE number =:number")
    fun getFavouriteContactsByNumber(number: String): Contact

    @Query("SELECT * FROM CallHistory WHERE number =:number")
    fun getCallHistoryByNumber(number: String): CallHistory

    @Query("UPDATE CallHistory SET date=:date,SimName=:SimName WHERE id = :id")
    fun updateCallHistory(date: String, SimName: String, id: Int)

    @Query("UPDATE CallHistory SET name=:name WHERE id = :id")
    fun updateNameCallHistory(name: String, id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAutoCallContacts(autoCallContacts: AutoCallContacts)

    @Query("SELECT * FROM AutoCallContacts")
    fun getAutoCallContacts():List<AutoCallContacts>
    @Query("UPDATE AutoCallContacts SET isAutoDialer=:isAutoDialer WHERE id = :id")
    fun updateIsAutoDialerContact(isAutoDialer: Boolean, id: Int)
    @Query("UPDATE AutoCallContacts SET isAnswerCall=:isAnswerCall WHERE id = :id")
    fun updateIsAnswerCallAutoCallContacts(isAnswerCall: Boolean, id: Int)
}