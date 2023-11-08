package com.cartravelsdailerapp.models

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity
data class FavouritesContacts(
    @ColumnInfo
    val name: String,
    @ColumnInfo
    val number: String,
    @ColumnInfo
    val photoUri: String,
    @ColumnInfo
    val contactId: String,
    @ColumnInfo
    val isFavourites: Boolean
)