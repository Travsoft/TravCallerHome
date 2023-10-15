package com.cartravelsdailerapp.viewmodels

import android.app.Application
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.os.Build
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

class CallHistoryViewmodel(
    var context: Application,
    private val callLogsRepository: CallLogsRepository

) : AndroidViewModel(context) {
    private val _callLogsByNumber = MutableLiveData<List<CallHistory>>()
    val callLogsByNumber: LiveData<List<CallHistory>>
        get() = _callLogsByNumber

    suspend fun getCallLogsHistoryByNumber(number: String): List<CallHistory> {
        return callLogsRepository.fetchCallLogs().filter { it.number.equals(number) }
            .sortedByDescending {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    SimpleDateFormat(PrefUtils.DataFormate).parse(it.date)
                } else {
                    TODO("VERSION.SDK_INT < N")
                }
            }
    }

}