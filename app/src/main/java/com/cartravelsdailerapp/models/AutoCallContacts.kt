package com.cartravelsdailerapp.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AutoCallContacts(
    @ColumnInfo
    val name: String,
    @ColumnInfo
    val phoneNumber: String,
    @ColumnInfo
    val isAnswerCall: Boolean,
    @ColumnInfo
    val isAutoDialer: Boolean
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
