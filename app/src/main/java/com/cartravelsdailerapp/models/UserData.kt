package com.cartravelsdailerapp.models

import com.google.gson.annotations.SerializedName

data class UserData(
    @SerializedName("id") var id: String? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("email") var email: String? = null,
    @SerializedName("pinCode") var pinCode: String? = null,
    @SerializedName("phoneNumber") var phoneNumber: Float? = null,
    @SerializedName("state") var state: String? = null,
    @SerializedName("district") var district: String? = null,
    @SerializedName("city") var city: String? = null,
    @SerializedName("webLink") var webLink: String? = null,
    @SerializedName("alternateNumber") var alternateNumber: String? = null,
    @SerializedName("profilePicture") var profilePicture: String? = null,
    @SerializedName("createdAt") var createdAt: String? = null,
    @SerializedName("businessRegister") var businessRegister: Boolean? = null,
    @SerializedName("token") var token: String? = null
)
