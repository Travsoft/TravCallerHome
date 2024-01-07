package com.cartravelsdailerapp.Repositorys

import com.cartravelsdailerapp.interfaces.IBusinessDailerApi
import com.cartravelsdailerapp.models.UserExistRequest
import com.cartravelsdailerapp.models.UserExistResponse
import retrofit2.Response

class UserRepository{
    suspend fun userExist(loginRequest: UserExistRequest): Response<UserExistResponse>? {
        return  IBusinessDailerApi.getApi()?.userExist(loginRequest)
    }
}
