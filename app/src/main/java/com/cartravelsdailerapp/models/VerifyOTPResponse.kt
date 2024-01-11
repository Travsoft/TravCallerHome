package com.cartravelsdailerapp.models

import com.google.gson.annotations.SerializedName

data class VerifyOTPResponse(
    var message: String,
    var statusCode: Int,
    var success: Boolean
)