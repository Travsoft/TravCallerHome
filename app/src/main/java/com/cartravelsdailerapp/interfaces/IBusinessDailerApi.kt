package com.cartravelsdailerapp.interfaces

import com.cartravelsdailerapp.ApiClient
import com.cartravelsdailerapp.models.*
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

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

    @GET("/api/v1/user/getUser")
    suspend fun getUserByToken(@Header("Authorization") userToken: String): Response<UserResponse>

    @GET("/api/v1/cities/states")
    suspend fun getStates(
        @Header("Authorization") userToken: String
    ): Response<StatesResponse>

    @GET("/api/v1/cities/districts")
    suspend fun getDistricts(
        @Header("Authorization") userToken: String,
        @Query("state") state: String
    ): Response<DistrictsResponse>

    @GET("/api/v1/cities/cities")
    suspend fun getCities(
        @Header("Authorization") userToken: String,
        @Query("district") state: String
    ): Response<CitiesResponse>

    @GET("/api/v1/cities/mandal")
    suspend fun getMandal(
        @Header("Authorization") userToken: String,
        @Query("city") state: String
    ): Response<MandalResponse>
    @GET("/api/v1/cities/village")
    suspend fun getVillage(
        @Header("Authorization") userToken: String,
        @Query("mandal") state: String
    ): Response<VillageResponse>

    @PATCH("/api/v1/user/updateUser")
    suspend fun updateUser(
        @Header("Authorization") userToken: String,
        @Body body: RequestBody
    ): Response<UserUpdateResponse>

    companion object {
        fun getApi(): IBusinessDailerApi? {
            return ApiClient.client?.create(IBusinessDailerApi::class.java)
        }
    }
}