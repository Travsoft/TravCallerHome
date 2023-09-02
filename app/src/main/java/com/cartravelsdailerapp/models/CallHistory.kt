package com.cartravelsdailerapp.models

data class CallHistory(
    var calType: String,
    var number: String,
    var name: String?,
    var type: Int,
    var date: String,
    var duration: Long,
    var subscriberId: String,
    var photouri: String,
    var SimName:String
) {

    val cachedName: String  // Separate property for cachedName
        get() {
            return if (name == null) number else name as String
        }

}
