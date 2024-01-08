package com.cartravelsdailerapp.models

data class UserRegisterResponse(val msg:String,val statusCode:Int,val success:Boolean,val data:List<UserData>)
