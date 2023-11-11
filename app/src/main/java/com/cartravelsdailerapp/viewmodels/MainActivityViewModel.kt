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
import kotlinx.coroutines.*
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

    suspend fun getNewCallLogsHistory() {
        viewModelScope.launch {
            _newCallLogs.value = callLogsRepository.fetchCallLogSignle()
            _newCallLogs.value?.let { db.insertCallHistory(it) }
        }
    }

    fun getCallLogsHistoryDb() {
        _callLogsdb.value = db.getAllCallLogs()
    }

    fun getAllFavouriteContacts() {
        viewModelScope.launch {
            _AllFavouriteContacts.value = db.getAllFavouriteContacts(true)
        }
    }

    fun getContacts():List<Contact> {
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

