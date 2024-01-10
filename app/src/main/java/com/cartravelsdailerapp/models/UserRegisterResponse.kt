package com.cartravelsdailerapp.models

data class UserRegisterResponse(
    val data: List<UserData>,
    val msg: String,
    val statusCode: Int,
    val success: Boolean
)
