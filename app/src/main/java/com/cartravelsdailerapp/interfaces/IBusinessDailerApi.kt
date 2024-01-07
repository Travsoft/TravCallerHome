package com.cartravelsdailerapp.interfaces

import com.cartravelsdailerapp.ApiClient
import com.cartravelsdailerapp.models.UserExistRequest
import com.cartravelsdailerapp.models.UserExistResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface IBusinessDailerApi {
    @POST("/api/v1/auth/userAlreadyExists")
    suspend fun userExist(@Body userExistRequest: UserExistRequest): Response<UserExistResponse>


    companion object {
        fun getApi(): IBusinessDailerApi? {
            return ApiClient.client?.create(IBusinessDailerApi::class.java)
        }
    }
}