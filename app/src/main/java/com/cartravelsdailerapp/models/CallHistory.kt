package com.cartravelsdailerapp.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CallHistory(
    @ColumnInfo
    var calType: String,
    @ColumnInfo
    var number: String,
    @ColumnInfo
    var name: String?,
    @ColumnInfo
    var type: Int,
    @ColumnInfo
    var date: String,
    @ColumnInfo
    var duration: String,
    @ColumnInfo
    var subscriberId: String,
    @ColumnInfo
    var photouri: String,
    @ColumnInfo
    var SimName: String,
    @ColumnInfo
    var lookUpUri: String
) {
    @ColumnInfo
    var IsExpand: Boolean = false

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    @ColumnInfo
    var Count: Int = 0
    val cachedName: String  // Separate property for cachedName
        get() {
            return if (name == null) number else name as String
        }

}
