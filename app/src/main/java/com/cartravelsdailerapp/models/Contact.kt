package com.cartravelsdailerapp.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Contact(
    @ColumnInfo
    val name: String,
    @ColumnInfo
    val number: String,
    @ColumnInfo
    val photoUri: String,
    @ColumnInfo
    val contactId:String,
    @ColumnInfo
    val isFavourites: Boolean
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

}