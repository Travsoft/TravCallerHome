package com.cartravelsdailerapp.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.cartravelsdailerapp.Repositorys.CallLogsRepository
import com.cartravelsdailerapp.db.DatabaseBuilder
import com.cartravelsdailerapp.models.CallHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class CallHistoryViewmodel(
    var context: Application,
    private val callLogsRepository: CallLogsRepository

) : AndroidViewModel(context) {
    private val _callLogsByNumber = MutableLiveData<List<CallHistory>>()
    val callLogsByNumber: LiveData<List<CallHistory>>
        get() = _callLogsByNumber

     fun getCallLogsHistoryByNumber(number: String) {
        viewModelScope.launch(Dispatchers.Main) {
            async {
                _callLogsByNumber.value =
                    callLogsRepository.fetchCallLogs().filter{it.number.equals(number)}
            }
        }
    }

}