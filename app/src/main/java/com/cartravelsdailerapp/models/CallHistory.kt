package com.cartravelsdailerapp.models

data class CallHistory(
    var calType: String,
    var number: String,
    var name: String?,
    var type: Int,
    var date: String,
    var duration: String,
    var subscriberId: String,
    var photouri: String,
    var SimName: String
) {
    var IsExpand: Boolean = false
    var Count: Int = 0
    val cachedName: String  // Separate property for cachedName
        get() {
            return if (name == null) number else name as String
        }

}
