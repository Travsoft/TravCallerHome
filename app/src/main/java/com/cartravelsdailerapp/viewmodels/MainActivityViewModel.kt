package com.cartravelsdailerapp.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.cartravelsdailerapp.Repositorys.CallLogsRepository
import com.cartravelsdailerapp.db.DatabaseBuilder
import com.cartravelsdailerapp.models.CallHistory
import kotlinx.coroutines.launch
import java.util.*

class MainActivityViewModel(
    var context: Application,
    private val callLogsRepository: CallLogsRepository
) : AndroidViewModel(context) {
    private val _callLogs = MutableLiveData<List<CallHistory>>()
    val callLogs: LiveData<List<CallHistory>>
        get() = _callLogs

     fun getCallLogsHistory() {
        viewModelScope.launch {
            _callLogs.value = callLogsRepository.fetchCallLogs().distinctBy { i ->
                {
                    i.number
                }
            }
            DatabaseBuilder.getInstance(context).CallHistoryDao().insertAll(_callLogs.value!!)
        }
    }

    fun getAllCallLogsHistory(): List<CallHistory> {
        return DatabaseBuilder.getInstance(context).CallHistoryDao().getAll()
    }
}

