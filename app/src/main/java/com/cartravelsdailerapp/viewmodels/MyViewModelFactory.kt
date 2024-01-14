package com.cartravelsdailerapp.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers

class MyViewModelFactory(private val application: Application) :
    ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            LoginAndSignUpViewModel::class.java -> {
                LoginAndSignUpViewModel(
                    application
                ) as T

            }
            ProfileViewModel::class.java -> {
                ProfileViewModel(
                    application
                ) as T

            }
            else -> {
                throw IllegalArgumentException("Unknown ViewModel class")
            }

        }
    }
}