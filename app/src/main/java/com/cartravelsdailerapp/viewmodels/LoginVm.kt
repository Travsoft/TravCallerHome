package com.cartravelsdailerapp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cartravelsdailerapp.BaseResponse
import com.cartravelsdailerapp.Repositorys.CallLogsRepository
import com.cartravelsdailerapp.Repositorys.UserRepository
import com.cartravelsdailerapp.models.UserExistRequest
import kotlinx.coroutines.launch

class LoginVm(
    context: Application
) : AndroidViewModel(context) {

}