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
                Log.d("insert data-->", _callLogs.value!!.count().toString())
                _isCallLogsdb.value = true
            }
        }
    }

    fun getNewCallLogsHistory(number: String, simName: String) {

        viewModelScope.launch {
            val callhistoryData = db.getAllCallLogs()
            val callHistory =
                callhistoryData.find { it.number == number }
            if (callHistory == null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val simpDate = SimpleDateFormat(PrefUtils.DataFormate)
                    val date = Date()
                    val current = simpDate.format(date)
                    db.insertCallHistory(
                        CallHistory(
                            "OUTGOING",
                            number,
                            "",
                            0,
                            current.toString(),
                            "",
                            "",
                            "",
                            simName
                        )
                    )
                    Log.d("92", "inserted ${number}")
                } else {
                    val d = Date()
                    db.insertCallHistory(
                        CallHistory(
                            "OUTGOING",
                            number,
                            "",
                            0,
                            d.toString(),
                            "",
                            "",
                            "",
                            simName
                        )
                    )
                    Log.d("107", "inserted ${number}")

                }
                Log.d("inserted 108", number)
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val simpDate = SimpleDateFormat(PrefUtils.DataFormate)
                    val date = Date()
                    val current = simpDate.format(date)
                    db.updateCallHistory(
                        current, callHistory.id
                    )
                    Log.d(
                        "updated 84", " ${callHistory.id} ${
                            callHistory
                                .number
                        }"
                    )
                    Log.d("updated 125", "inserted ${number}")

                } else {
                    val d = Date()
                    db.updateCallHistory(
                        d.toString(), callHistory.id
                    )
                    Log.d("updated 91", " ${callHistory.id}")

                }
                Log.d("inserted 132", number)

            }
            getCallLogsHistoryDb()
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

