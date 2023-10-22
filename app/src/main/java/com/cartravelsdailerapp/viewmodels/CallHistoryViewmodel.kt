package com.cartravelsdailerapp.viewmodels

import android.app.Application
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.cartravelsdailerapp.PrefUtils
import com.cartravelsdailerapp.Repositorys.CallLogsRepository
import com.cartravelsdailerapp.db.DatabaseBuilder
import com.cartravelsdailerapp.models.CallHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CallHistoryViewmodel(
    var context: Application,
    private val callLogsRepository: CallLogsRepository

) : AndroidViewModel(context) {
    private val _callLogsByNumber = MutableLiveData<List<CallHistory>>()
    val callLogsByNumber: LiveData<List<CallHistory>>
        get() = _callLogsByNumber
    var db = DatabaseBuilder.getInstance(context).CallHistoryDao()

    fun getCallLogsHistoryByNumber(number: String): List<CallHistory>? {
        viewModelScope.launch {
            _callLogsByNumber.value = db.callDataByNumber(number).sortedByDescending {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    SimpleDateFormat(PrefUtils.DataFormate).parse(it.date)
                } else {
                    TODO("VERSION.SDK_INT < N")
                }
            }
        }
        return _callLogsByNumber.value
    }

}