package com.cartravelsdailerapp.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.cartravelsdailerapp.Repositorys.CallLogsRepository
import com.cartravelsdailerapp.models.CallHistory
import kotlinx.coroutines.launch
import java.util.*

class MainActivityViewModel(
    context: Application,
    private val callLogsRepository: CallLogsRepository
) : AndroidViewModel(context) {

    private val _callLogs = MutableLiveData<List<CallHistory>>()
    val callLogs: LiveData<List<CallHistory>>
        get() = _callLogs

    init {
        getCallLogs()
    }

    private fun getCallLogs() {
        viewModelScope.launch {
            _callLogs.value = callLogsRepository.fetchCallLogs().distinctBy { i ->
                {
                    i.number
                }
            }
        }
    }
}

