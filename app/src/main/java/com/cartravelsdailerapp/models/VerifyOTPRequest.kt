package com.cartravelsdailerapp.models

import com.google.gson.annotations.SerializedName

data class VerifyOTPRequest(
    @SerializedName("email") var email: String? = null,
    @SerializedName("otp") var otp: String? = null,
    @SerializedName("newPassword") var newPassword: String? = null
)
