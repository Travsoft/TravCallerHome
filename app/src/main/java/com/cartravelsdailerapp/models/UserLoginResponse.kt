package com.cartravelsdailerapp.models

import com.google.gson.annotations.SerializedName

data class UserLoginResponse(
    @SerializedName("data") var data: ArrayList<UserData> = arrayListOf(),
    @SerializedName("msg") var msg: String? = null,
    @SerializedName("statusCode") var statusCode: Int? = null,
    @SerializedName("success") var success: Boolean? = null
)
