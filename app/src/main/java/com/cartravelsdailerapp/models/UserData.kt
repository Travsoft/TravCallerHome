package com.cartravelsdailerapp.models

data class UserData(
    val id: String,
    val name: String,
    val email: String,
    val pinCode: Int,
    val phoneNumber: Int,
    val state: String,
    val district: String,
    val city: String,
    val webLink: String,
    val alternateNumber: String,
    val profilePicture: String,
    val businessDetails: UserBusinessDetails,
    val createdAt: String,
    val token: String,
)
