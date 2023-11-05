package com.cartravelsdailerapp.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.alexstyl.contactstore.ContactStore
import com.alexstyl.contactstore.thumbnailUri
import com.cartravelsdailerapp.Repositorys.CallLogsRepository
import com.cartravelsdailerapp.db.DatabaseBuilder
import com.cartravelsdailerapp.models.CallHistory
import com.cartravelsdailerapp.models.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivityViewModel(
    var context: Application,
    private val callLogsRepository: CallLogsRepository
) : AndroidViewModel(context) {
    private val _isCallLogsdb = MutableLiveData<Boolean>()
    val IsCallLogsdb: LiveData<Boolean>
        get() = _isCallLogsdb
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
    lateinit var listOfContactStore: ContactStore

    suspend fun getCallLogsHistory() {
        viewModelScope.launch {
            _callLogs.value = callLogsRepository.fetchCallLogs()
            withContext(Dispatchers.Main) {
                db.insertAll(_callLogs.value!!)
                Log.d("insert data-->", _callLogs.value!!.count().toString())
                _isCallLogsdb.value = true
            }
        }
    }

    suspend fun getNewCallLogsHistory() {
        viewModelScope.launch {
            _newCallLogs.value = callLogsRepository.fetchCallLogSignle()
            _newCallLogs.value?.let { db.insertCallHistory(it) }
        }
    }
}

