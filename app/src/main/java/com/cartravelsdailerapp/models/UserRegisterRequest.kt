package com.cartravelsdailerapp.models

data class UserRegisterRequest(
    val profilePicture: String,
    val name: String,
    val email: String,
    val pinCode: String,
    val phoneNumber: String,
    val password: String,
    val state: String,
    val district: String,
    val city: String,
    val webLink: String,
    val alternateNumber: String,
    val jobTitle: String,
    val companyName: String,
)
