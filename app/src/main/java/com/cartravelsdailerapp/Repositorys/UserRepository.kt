package com.cartravelsdailerapp.Repositorys

import com.cartravelsdailerapp.interfaces.IBusinessDailerApi
import com.cartravelsdailerapp.models.*
import okhttp3.RequestBody
import retrofit2.Response

class UserRepository {
    suspend fun userExist(loginRequest: UserExistRequest): Response<UserExistResponse>? {
        return IBusinessDailerApi.getApi()?.userExist(loginRequest)
    }

    suspend fun userLogin(loginRequest: UserLoginRequest): Response<UserLoginResponse>? {
        return IBusinessDailerApi.getApi()?.userLogin(loginRequest)
    }

    suspend fun userRegister(requestBody: RequestBody): Response<UserRegisterResponse>? {
        return IBusinessDailerApi.getApi()?.userRegister(requestBody)
    }
}
