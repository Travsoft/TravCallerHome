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
    private val _newCallLogs = MutableLiveData<CallHistory>()
    val newCallLogs: LiveData<CallHistory>
        get() = _newCallLogs
    var db = DatabaseBuilder.getInstance(context).CallHistoryDao()
    fun getCallLogsHistory() {
        viewModelScope.launch {
            _callLogs.value = callLogsRepository.fetchCallLogs().distinctBy { i ->
                {
                    i.number
                }
            }
            db.insertAll(_callLogs.value!!)
        }
    }

    fun getAllCallLogsHistory(): List<CallHistory> {
        return DatabaseBuilder.getInstance(context).CallHistoryDao().getAll()
    }

    fun getNewCallLogsHistory(): CallHistory {
        viewModelScope.launch {
            _newCallLogs.value = callLogsRepository.fetchCallLogSignle()
            db.insertCallHistory(_newCallLogs.value!!)
        }
        return callLogsRepository.fetchCallLogSignle()
    }
}
