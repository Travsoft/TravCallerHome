package com.cartravelsdailerapp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.cartravelsdailerapp.BaseResponse
import com.cartravelsdailerapp.Repositorys.UserRepository
import com.cartravelsdailerapp.models.UserLoginRequest
import com.cartravelsdailerapp.models.UserLoginResponse
import com.cartravelsdailerapp.models.UserRegisterRequest
import com.cartravelsdailerapp.models.UserRegisterResponse
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File


class LoginAndSignUpViewModel(
    var context: Application
) : AndroidViewModel(context) {
    private val userRepo = UserRepository()
    val userLoginResp: MutableLiveData<BaseResponse<UserLoginResponse>> = MutableLiveData()
    val userData: MutableLiveData<BaseResponse<UserRegisterResponse>> = MutableLiveData()

    fun userLogin(email: String, password: String) {
        viewModelScope.launch {
            try {

                val loginRequest = UserLoginRequest(
                    email = email,
                    password = password
                )
                val response = userRepo.userLogin(loginRequest = loginRequest)
                if (response?.code() == 200) {
                    userLoginResp.value = BaseResponse.Success(response.body())
                } else {
                    userLoginResp.value = BaseResponse.Error(response?.message())
                }

            } catch (ex: Exception) {
                userLoginResp.value = BaseResponse.Error(ex.message)
            }
        }
    }

    fun userRegister(userRegister: UserRegisterRequest, file: File?) {
        viewModelScope.launch {
            try {

                val requestBody: RequestBody =
                    file?.let { RequestBody.create("image/*".toMediaTypeOrNull(), it) }?.let {
                        MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("name", userRegister.name)
                            .addFormDataPart("email", userRegister.email)
                            .addFormDataPart("pinCode", userRegister.pinCode)
                            .addFormDataPart("phoneNumber", userRegister.phoneNumber)
                            .addFormDataPart("password", userRegister.password)
                            .addFormDataPart("state", userRegister.state)
                            .addFormDataPart("district", userRegister.district)
                            .addFormDataPart("city", userRegister.city)
                            .addFormDataPart("webLink", userRegister.webLink)
                            .addFormDataPart("alternateNumber", userRegister.alternateNumber)
                            .addFormDataPart("jobTitle", userRegister.jobTitle)
                            .addFormDataPart("companyName", userRegister.companyName)
                            .addFormDataPart(
                                "profilePicture", file.name, it
                            )
                            .build()
                    }!!

                val response = userRepo.userRegister(requestBody)
                if (response?.code() == 200) {
                    userData.value = BaseResponse.Success(response.body())
                } else {
                    userData.value = BaseResponse.Error(response?.message())
                }

            } catch (ex: Exception) {
                userData.value = BaseResponse.Error(ex.message)
            }
        }

    }


}