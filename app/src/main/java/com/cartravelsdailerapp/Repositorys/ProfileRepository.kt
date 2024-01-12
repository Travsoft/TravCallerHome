package com.cartravelsdailerapp.Repositorys

import com.cartravelsdailerapp.interfaces.IBusinessDailerApi
import com.cartravelsdailerapp.models.UserResponse
import com.cartravelsdailerapp.models.UserUpdateResponse
import okhttp3.RequestBody
import retrofit2.Response

class ProfileRepository() {

    suspend fun userData(token: String): Response<UserResponse>? {
        return IBusinessDailerApi.getApi()?.getUserByToken(token)
    }

    suspend fun userData(token: String, requestBody: RequestBody): Response<UserUpdateResponse>? {
        return IBusinessDailerApi.getApi()?.updateUser(token, requestBody)
    }
}