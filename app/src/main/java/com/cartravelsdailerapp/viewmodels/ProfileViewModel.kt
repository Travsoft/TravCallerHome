package com.cartravelsdailerapp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.cartravelsdailerapp.BaseResponse
import com.cartravelsdailerapp.Repositorys.ProfileRepository
import com.cartravelsdailerapp.models.UserResponse
import com.cartravelsdailerapp.models.UserUpdateResponse
import kotlinx.coroutines.launch
import okhttp3.RequestBody

class ProfileViewModel(
    var context: Application,
) : AndroidViewModel(context) {
    private val profileRepository = ProfileRepository()

    val userDataResp: MutableLiveData<BaseResponse<UserResponse>> = MutableLiveData()
    val userUpdateDataResp: MutableLiveData<BaseResponse<UserUpdateResponse>> = MutableLiveData()

    fun getUserDataByToken(token: String) {
        viewModelScope.launch {
            try {
                val response = profileRepository.userData(token)
                if (response?.code() == 200 || response?.code() == 400) {
                    userDataResp.value = BaseResponse.Success(response.body())
                } else {
                    userDataResp.value = BaseResponse.Error(response?.message())
                }

            } catch (ex: Exception) {
                userDataResp.value = BaseResponse.Error(ex.message)
            }
        }
    }

    fun updateUserDataByToken(token: String, requestBody: RequestBody) {
        viewModelScope.launch {
            try {
                val response = profileRepository.updateUser(token, requestBody)
                if (response?.code() == 200 || response?.code() == 400) {
                    userUpdateDataResp.value = BaseResponse.Success(response.body())
                } else {
                    userUpdateDataResp.value = BaseResponse.Error(response?.message())
                }

            } catch (ex: Exception) {
                userUpdateDataResp.value = BaseResponse.Error(ex.message)
            }
        }
    }

}