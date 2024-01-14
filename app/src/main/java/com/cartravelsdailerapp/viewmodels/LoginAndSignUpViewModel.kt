package com.cartravelsdailerapp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.cartravelsdailerapp.BaseResponse
import com.cartravelsdailerapp.Repositorys.UserRepository
import com.cartravelsdailerapp.models.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File


class LoginAndSignUpViewModel(
    var context: Application
) : AndroidViewModel(context) {
    private val userRepo = UserRepository()
    val verifyOTPResp: MutableLiveData<BaseResponse<VerifyOTPResponse>> = MutableLiveData()
    val sendOTPResp: MutableLiveData<BaseResponse<SendOTPResponse>> = MutableLiveData()
    val userLoginResp: MutableLiveData<BaseResponse<UserLoginResponse>> = MutableLiveData()
    val userData: MutableLiveData<BaseResponse<UserRegisterResponse>> = MutableLiveData()
    val getStatusResp: MutableLiveData<BaseResponse<StatesResponse>> = MutableLiveData()
    val getDistrictResp: MutableLiveData<BaseResponse<DistrictsResponse>> = MutableLiveData()
    val getCityResp: MutableLiveData<BaseResponse<CitiesResponse>> = MutableLiveData()
    val getMandalResp: MutableLiveData<BaseResponse<MandalResponse>> = MutableLiveData()

    fun sendOtp(email: String) {
        viewModelScope.launch {
            try {

                val sendOTPRequest = SendOTPRequest(
                    email = email
                )
                val response = userRepo.sendOTP(sendOTPRequest)
                if (response?.code() == 200 || response?.code() == 400) {
                    sendOTPResp.value = BaseResponse.Success(response.body())
                } else {
                    sendOTPResp.value = BaseResponse.Error(response?.message())
                }

            } catch (ex: Exception) {
                sendOTPResp.value = BaseResponse.Error(ex.message)
            }
        }
    }

    fun verifyOTP(email: String, newPassword: String, otp: String) {
        viewModelScope.launch {
            try {
                val sendOTPRequest = VerifyOTPRequest(
                    email = email,
                    newPassword = newPassword,
                    otp = otp.toInt()
                )
                val response = userRepo.verifyOTP(sendOTPRequest)
                if (response?.code() == 200 || response?.code() == 400) {
                    verifyOTPResp.value = BaseResponse.Success(response.body())
                } else {
                    verifyOTPResp.value = BaseResponse.Error(response?.message())
                }

            } catch (ex: Exception) {
                verifyOTPResp.value = BaseResponse.Error(ex.message)
            }
        }
    }

    fun userLogin(email: String, password: String) {
        viewModelScope.launch {
            try {

                val loginRequest = UserLoginRequest(
                    phoneNumber = email,
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

    fun getStates(token: String) {
        viewModelScope.launch {
            try {
                val response = userRepo.getStates(token)
                if (response?.code() == 200) {
                    getStatusResp.value = BaseResponse.Success(response.body())
                } else {
                    getStatusResp.value = BaseResponse.Error(response?.message())
                }

            } catch (ex: Exception) {
                getStatusResp.value = BaseResponse.Error(ex.message)
            }
        }
    }

    fun getDistrict(token: String, selectedState: String) {
        viewModelScope.launch {
            try {
                val response = userRepo.getDistricts(token,selectedState)
                if (response?.code() == 200) {
                    getDistrictResp.value = BaseResponse.Success(response.body())
                } else {
                    getDistrictResp.value = BaseResponse.Error(response?.message())
                }

            } catch (ex: Exception) {
                getStatusResp.value = BaseResponse.Error(ex.message)
            }
        }
    }
    fun getCities(token: String, seletedDistrict: String) {
        viewModelScope.launch {
            try {
                val response = userRepo.getCities(token,seletedDistrict)
                if (response?.code() == 200) {
                    getCityResp.value = BaseResponse.Success(response.body())
                } else {
                    getCityResp.value = BaseResponse.Error(response?.message())
                }

            } catch (ex: Exception) {
                getCityResp.value = BaseResponse.Error(ex.message)
            }
        }
    }
    fun getMandal(token: String, seletedCity: String) {
        viewModelScope.launch {
            try {
                val response = userRepo.getMandal(token,seletedCity)
                if (response?.code() == 200) {
                    getMandalResp.value = BaseResponse.Success(response.body())
                } else {
                    getMandalResp.value = BaseResponse.Error(response?.message())
                }

            } catch (ex: Exception) {
                getMandalResp.value = BaseResponse.Error(ex.message)
            }
        }
    }


}