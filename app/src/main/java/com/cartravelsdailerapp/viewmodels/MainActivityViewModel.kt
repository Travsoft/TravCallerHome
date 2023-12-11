package com.cartravelsdailerapp.viewmodels

import android.app.Application
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.provider.CallLog
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import com.alexstyl.contactstore.ContactStore
import com.alexstyl.contactstore.thumbnailUri
import com.cartravelsdailerapp.PrefUtils
import com.cartravelsdailerapp.Repositorys.CallLogsRepository
import com.cartravelsdailerapp.db.DatabaseBuilder
import com.cartravelsdailerapp.models.CallHistory
import com.cartravelsdailerapp.models.Contact
import kotlinx.coroutines.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.CoroutineContext

class MainActivityViewModel(
    var context: Application,
    private val callLogsRepository: CallLogsRepository
) : AndroidViewModel(context), CoroutineScope {
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

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

    private val _AllFavouriteContacts = MutableLiveData<List<Contact>>()
    val AllFavouriteContacts: LiveData<List<Contact>>
        get() = _AllFavouriteContacts

    private val _AllContacts = MutableLiveData<List<Contact>>()
    val AllContacts: LiveData<List<Contact>>
        get() = _AllContacts

    var db = DatabaseBuilder.getInstance(context).CallHistoryDao()
    val listOfContactStore = ContactStore.newInstance(context)

    suspend fun getCallLogsHistory() {
        viewModelScope.launch {
            _callLogs.value = callLogsRepository.fetchCallLogs()
            withContext(Dispatchers.Main) {
                db.insertAll(_callLogs.value!!)
                _isCallLogsdb.value = true
            }
        }
    }

    fun getNewCallLogsHistory(number: String, simName: String) {
        if (number.isNotBlank() && simName.isNotBlank()) {
            viewModelScope.launch {
                val callLogs = db.getAllCallLogs()

                val callHistory = callLogs.find {
                    it.number.equals(number)
                }
                if (callHistory == null) {
                    val callhistoryData = callLogsRepository.fetchCallLogSignle(number)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        val simpDate = SimpleDateFormat(PrefUtils.DataFormate)
                        val date = Date()
                        val current = simpDate.format(date)
                        var name = ""
                        if (callhistoryData.name.isNullOrEmpty()) {
                            name = callhistoryData.number
                        } else {
                            name = callhistoryData.name!!
                        }
                        db.insertCallHistory(
                            CallHistory(
                                callhistoryData.calType,
                                callhistoryData.number,
                                name,
                                callhistoryData.type,
                                current.toString(),
                                callhistoryData.duration,
                                callhistoryData.subscriberId,
                                callhistoryData.photouri,
                                simName
                            )
                        )
                    } else {
                        val d = Date()
                        db.insertCallHistory(
                            CallHistory(
                                callhistoryData.calType,
                                callhistoryData.number,
                                callhistoryData.name,
                                callhistoryData.type,
                                d.toString(),
                                callhistoryData.duration,
                                callhistoryData.subscriberId,
                                callhistoryData.photouri,
                                simName
                            )
                        )
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        val simpDate = SimpleDateFormat(PrefUtils.DataFormate)
                        val date = Date()
                        val current = simpDate.format(date)
                        db.updateCallHistory(
                            current, simName, callHistory.id
                        )
                    } else {
                        val d = Date()
                        db.updateCallHistory(
                            d.toString(), simName, callHistory.id
                        )
                    }
                }
                getCallLogsHistoryDb()
            }
        }
    }

    fun getCallLogsHistoryDb() {
        _callLogsdb.value = db.getAllCallLogs().sortedByDescending {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                SimpleDateFormat(PrefUtils.DataFormate).parse(it.date)
            } else {
                TODO("VERSION.SDK_INT < N")
            }
        }
    }

    fun getAllFavouriteContacts() {
        viewModelScope.launch {
            _AllFavouriteContacts.value = db.getAllFavouriteContacts(true)
        }
    }

    fun getContacts(): List<Contact> {
        job = Job()
        val list = ArrayList<Contact>()
        viewModelScope.launch(Dispatchers.Main) {
            listOfContactStore.fetchContacts().collect { it ->
                it.forEach {
                    if (!it.displayName.isNullOrBlank()) {
                        list.add(
                            Contact(
                                it.displayName,
                                "",
                                it.thumbnailUri.toString(),
                                contactId = it.contactId.toString(),
                                isFavourites = it.isStarred
                            )
                        )
                    }

                }
            }
        }
        return list

    }

}

