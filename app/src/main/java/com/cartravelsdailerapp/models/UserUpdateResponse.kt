package com.cartravelsdailerapp.models

data class UserUpdateResponse(
    val `data`: List<Data>,
    val msg: String,
    val statusCode: Int,
    val success: Boolean
)