package com.cartravelsdailerapp.viewmodels

import android.app.Application
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.util.Log
import androidx.lifecycle.*
import com.alexstyl.contactstore.ContactStore
import com.alexstyl.contactstore.thumbnailUri
import com.cartravelsdailerapp.BaseResponse
import com.cartravelsdailerapp.PrefUtils
import com.cartravelsdailerapp.Repositorys.CallLogsRepository
import com.cartravelsdailerapp.Repositorys.UserRepository
import com.cartravelsdailerapp.db.DatabaseBuilder
import com.cartravelsdailerapp.models.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext

class MainActivityViewModel(
    var context: Application,
    private val callLogsRepository: CallLogsRepository
) : AndroidViewModel(context), CoroutineScope {

    val userExistResp: MutableLiveData<BaseResponse<UserExistResponse>> = MutableLiveData()

    private val userRepo = UserRepository()

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
                    it.number == number
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
                                simName,
                                callhistoryData.lookUpUri
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
                                simName,
                                callhistoryData.lookUpUri
                            )
                        )
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        val simpleDateFormat = SimpleDateFormat(PrefUtils.DataFormate)
                        val date = simpleDateFormat.format(Date())
                        db.updateCallHistory(
                            date, simName, callHistory.id
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
        viewModelScope.launch(Dispatchers.Main) {
            _callLogsdb.value = db.getAllCallLogs().distinctBy {
                it.number
            }.sortedByDescending {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    SimpleDateFormat(PrefUtils.DataFormate).parse(it.date)
                } else {
                    TODO("VERSION.SDK_INT < N")
                }
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
                                isFavourites = it.isStarred,
                                contactsLookUp = it.lookupKey?.value.toString()
                            )
                        )
                    }

                }
            }
        }
        return list

    }

    fun userExist(email: String, phoneNum: String) {
        viewModelScope.launch {
            try {

                val loginRequest = UserExistRequest(
                    email = email,
                    phoneNumber = phoneNum
                )
                val response = userRepo.userExist(loginRequest = loginRequest)
                if (response?.code() == 200) {
                    userExistResp.value = BaseResponse.Success(response.body())
                } else {
                    userExistResp.value = BaseResponse.Error(response?.message())
                }

            } catch (ex: Exception) {
                userExistResp.value = BaseResponse.Error(ex.message)
            }
        }
    }


}

