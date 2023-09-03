package com.cartravelsdailerapp.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.icu.text.DateFormat
import android.os.Build
import android.os.Bundle
import android.provider.CallLog
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.cartravelsdailerapp.MainActivity
import com.cartravelsdailerapp.databinding.FragmentCallHistoryBinding
import com.cartravelsdailerapp.models.CallHistory
import com.cartravelsdailerapp.ui.adapters.CallHistoryAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.*


class CallHistoryFragment : Fragment() {
    @RequiresApi(Build.VERSION_CODES.N)
    val dateTimeFormat: DateFormat =
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
    var listOfCallHistroy: ArrayList<CallHistory> = ArrayList()
    lateinit var binding: FragmentCallHistoryBinding
    lateinit var adapter: CallHistoryAdapter
    var REQUESTED_CODE_READ_PHONE_STATE = 1003
    lateinit var calendar: Calendar

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        calendar = Calendar.getInstance()
        val dayOfWeekString =
            calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ENGLISH)
        // Inflate the layout for this fragment
        binding = FragmentCallHistoryBinding.inflate(layoutInflater)
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CALL_LOG
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_CALL_LOG
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            runBlocking {
                listOfCallHistroy.addAll(withContext(Dispatchers.Default) {
                    getAllCallHistory()
                })
                adapter =
                    CallHistoryAdapter(listOfCallHistroy, container!!.context.applicationContext)
                binding.txtNodataFound.isVisible = listOfCallHistroy.isEmpty()
                binding.recyclerViewCallHistory.isVisible = !listOfCallHistroy.isEmpty()
                binding.recyclerViewCallHistory.layoutManager = LinearLayoutManager(context)
                binding.recyclerViewCallHistory.adapter = adapter
            }
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.READ_PHONE_NUMBERS,
                    Manifest.permission.WRITE_CALL_LOG,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.CALL_PHONE
                ),
                REQUESTED_CODE_READ_PHONE_STATE
            )

        }
        return binding.root
    }

    /**
     * This Function Will return list of SubscriptionInfo
     */
    private fun getSimCardInfosBySubscriptionId(subscriptionId: String): SubscriptionInfo? {
        val subscriptionManager: SubscriptionManager =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                requireActivity().getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
            } else {
                TODO("VERSION.SDK_INT < LOLLIPOP_MR1")
            }
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                REQUESTED_CODE_READ_PHONE_STATE
            )

            return null
        } else {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                subscriptionManager.activeSubscriptionInfoList.find {
                    try {
                        it.subscriptionId == subscriptionId.toInt()
                    } catch (e: Exception) {
                        return null
                    }
                }
            } else {
                return null
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUESTED_CODE_READ_PHONE_STATE -> {
                if (grantResults.size > 0 && grantResults.all { it == 0 }) {
                    runBlocking {
                        MainActivity.listData.addAll(withContext(Dispatchers.Default) {
                            getAllCallHistory()
                        })
                    }

                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    @SuppressLint("Range")
    fun getAllCallHistory(): MutableList<CallHistory> {
        requireContext().contentResolver?.query(
            CallLog.Calls.CONTENT_URI, null, null,
            null, null
        )?.let {
            val callHistoryList = mutableListOf<CallHistory>()
            var dir: String? = null
            while (it.moveToNext()) {
                when (it.getString(it.getColumnIndex(CallLog.Calls.TYPE)).toInt()) {
                    CallLog.Calls.OUTGOING_TYPE -> dir = "OUTGOING"
                    CallLog.Calls.INCOMING_TYPE -> dir = "INCOMING"
                    CallLog.Calls.MISSED_TYPE -> dir = "MISSED"
                }
                callHistoryList.add(
                    CallHistory(
                        number = it.getString(it.getColumnIndex(CallLog.Calls.NUMBER)),
                        name = it.getString(it.getColumnIndex(CallLog.Calls.CACHED_NAME))
                            ?: null,
                        type = it.getString(it.getColumnIndex(CallLog.Calls.TYPE)).toInt(),
                        date = dateTimeFormat.format(it.getLong(it.getColumnIndex(CallLog.Calls.DATE)))
                            .toString(),
                        duration = it.getLong(it.getColumnIndex(CallLog.Calls.DURATION)).toString(),
                        subscriberId = it.getString(it.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID))
                            ?: "",
                        calType = dir.toString(),
                        photouri = it.getString(it.getColumnIndex(CallLog.Calls.CACHED_PHOTO_URI))
                            ?: "",
                        SimName = getSimCardInfosBySubscriptionId(
                            it.getString(it.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID))
                                ?: "0",
                        )?.displayName?.toString() ?: "",
                    )
                )

            }
            it.close()
            return callHistoryList.reversed().distinctBy { i -> i.name }.toMutableList()
        }

        return mutableListOf()

    }

}