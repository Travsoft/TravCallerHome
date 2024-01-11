package com.cartravelsdailerapp.models

import com.google.gson.annotations.SerializedName

/*
* {"message":"OTP Sent to email id  vishnunikhila11622@gmail.com","status code":200,"success":true}
* I/
*  {"message":"OTP Already sent to the email. Please kindly check the Email.  or  Try Agin after 5 minutes","statusCode":400,"success":false}

* */
data class SendOTPResponse(
    var message: String,
    var statusCode: Int,
    var success: Boolean,
)
