package com.cartravelsdailerapp.models

data class UserResponse(
    val alternateNumber: String,
    val businessRegister: Boolean,
    val city: String,
    val companyName: String,
    val district: String,
    val email: String,
    val id: String,
    val jobTitle: String,
    val msg: String,
    val name: String,
    val phoneNumber: Long,
    val pinCode: String,
    val profilePicture: String,
    val state: String,
    val statusCode: Int,
    val success: Boolean,
    val userBlocked: Boolean,
    val webLink: String
)