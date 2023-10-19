package com.cartravelsdailerapp.viewmodels

import android.app.Application
import android.icu.text.SimpleDateFormat
import android.os.Build
import androidx.lifecycle.*
import com.cartravelsdailerapp.PrefUtils
import com.cartravelsdailerapp.Repositorys.CallLogsRepository
import com.cartravelsdailerapp.db.DatabaseBuilder
import com.cartravelsdailerapp.models.CallHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class MainActivityViewModel(
    var context: Application,
    private val callLogsRepository: CallLogsRepository
) : AndroidViewModel(context) {
    private val _callLogsdb = MutableLiveData<List<CallHistory>>()
    val callLogsdb: LiveData<List<CallHistory>>
        get() = _callLogsdb

    private val _callLogs = MutableLiveData<List<CallHistory>>()
    val callLogs: LiveData<List<CallHistory>>
        get() = _callLogs
    private val _newCallLogs = MutableLiveData<CallHistory>()
    val newCallLogs: LiveData<CallHistory>
        get() = _newCallLogs
    var db = DatabaseBuilder.getInstance(context).CallHistoryDao()
    suspend fun getCallLogsHistory() {
        viewModelScope.launch {
            _callLogs.value = callLogsRepository.fetchCallLogs().sortedByDescending {
                (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    SimpleDateFormat(PrefUtils.DataFormate).parse(it.date)
                } else {
                    it.date
                }).toString()
            }.distinctBy { i ->
                i.number
            }
            withContext(Dispatchers.IO) {
                db.insertAll(_callLogs.value!!)
            }
        }
    }

    fun getAllCallLogsHistory(offset: Int): List<CallHistory> {
        return DatabaseBuilder.getInstance(context).CallHistoryDao().getAll(offset)
            .sortedByDescending {
                (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    SimpleDateFormat(PrefUtils.DataFormate).parse(it.date)
                } else {
                    it.date
                }).toString()
            }.distinctBy { i -> i.number }

    }

    suspend fun getNewCallLogsHistory(): CallHistory {
        viewModelScope.launch {
            _newCallLogs.value = callLogsRepository.fetchCallLogSignle()
            _newCallLogs.value?.let { db.insertCallHistory(it) }
        }
        return callLogsRepository.fetchCallLogSignle()
    }


}

