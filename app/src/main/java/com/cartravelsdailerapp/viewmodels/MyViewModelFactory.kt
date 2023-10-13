package com.cartravelsdailerapp.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cartravelsdailerapp.Repositorys.CallLogsRepository
import com.cartravelsdailerapp.Repositorys.DAO.CallLogsDataSource
import kotlinx.coroutines.Dispatchers

class MyViewModelFactory(private val application: Application) :
    ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            MainActivityViewModel::class.java -> {
                val source =
                    CallLogsDataSource(application.contentResolver, application)
                MainActivityViewModel(
                    application,
                    CallLogsRepository(source, Dispatchers.Default)
                ) as T
            }
            CallHistoryViewmodel::class.java -> {
                val source =
                    CallLogsDataSource(application.contentResolver, application)
                CallHistoryViewmodel(
                    application, CallLogsRepository(source, Dispatchers.Default)
                ) as T
            }
            else -> {
                throw IllegalArgumentException("Unknown ViewModel class")
            }

        }
    }
}