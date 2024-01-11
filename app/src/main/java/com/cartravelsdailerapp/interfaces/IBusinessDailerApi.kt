package com.cartravelsdailerapp.interfaces

import com.cartravelsdailerapp.ApiClient
import com.cartravelsdailerapp.models.*
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface IBusinessDailerApi {
    @POST("/api/v1/auth/userAlreadyExists")
    suspend fun userExist(@Body userExistRequest: UserExistRequest): Response<UserExistResponse>
    @POST("/api/v1/auth/login")
    suspend fun userLogin(@Body userLoginRequest: UserLoginRequest): Response<UserLoginResponse>
    @POST("/api/v1/auth/register")
    suspend fun userRegister(@Body body: RequestBody): Response<UserRegisterResponse>

    @POST("/api/v1/auth/sendOTP")
    suspend fun sendOTP(@Body body: SendOTPRequest): Response<SendOTPResponse>
    @POST("/api/v1/auth/verifyOTP")
    suspend fun verifyOTP(@Body body: VerifyOTPRequest): Response<VerifyOTPResponse>


    companion object {
        fun getApi(): IBusinessDailerApi? {
            return ApiClient.client?.create(IBusinessDailerApi::class.java)
        }
    }
}