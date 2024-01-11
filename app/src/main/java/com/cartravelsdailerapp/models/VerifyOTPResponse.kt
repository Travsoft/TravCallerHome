package com.cartravelsdailerapp.models

import com.google.gson.annotations.SerializedName

data class VerifyOTPResponse(
    @SerializedName("message") var message: String? = null,
    @SerializedName("statusCode") var statusCode: Int? = null,
    @SerializedName("success") var success: Boolean? = null
)