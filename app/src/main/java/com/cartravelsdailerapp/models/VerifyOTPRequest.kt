package com.cartravelsdailerapp.models

import com.google.gson.annotations.SerializedName

data class VerifyOTPRequest(
     var email: String,
     var otp: Int,
    var newPassword: String
)
