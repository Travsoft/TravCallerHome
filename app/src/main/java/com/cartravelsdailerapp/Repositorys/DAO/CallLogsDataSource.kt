package com.cartravelsdailerapp.Repositorys.DAO

import android.content.ContentResolver
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.provider.CallLog
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import androidx.annotation.RequiresApi
import com.cartravelsdailerapp.models.CallHistory
import java.util.*

class CallLogsDataSource(private val contentResolver: ContentResolver, val context: Context) {
    var simpDate = SimpleDateFormat("dd/MM/yyyy kk:mm");
    val callHistoryList = mutableListOf<CallHistory>()
    var dir: String? = null

    @RequiresApi(Build.VERSION_CODES.M)
    fun fetchCallLogsList(): List<CallHistory> {
        val cursor = contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(
                CallLog.Calls.TYPE,
                CallLog.Calls.NUMBER,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION,
                CallLog.Calls.PHONE_ACCOUNT_ID,
                CallLog.Calls.CACHED_PHOTO_URI
            ),
            null,
            null, null
        )
        while (cursor?.moveToNext() == true) {
            when (cursor.getColumnIndex(CallLog.Calls.TYPE).let { cursor.getString(it).toInt() }) {
                CallLog.Calls.OUTGOING_TYPE -> dir = "OUTGOING"
                CallLog.Calls.INCOMING_TYPE -> dir = "INCOMING"
                CallLog.Calls.MISSED_TYPE -> dir = "MISSED"
            }
            cursor?.getColumnIndex(CallLog.Calls.NUMBER)
                ?.let { cursor.getString(it) }?.let {
                    CallHistory(
                        number = it,
                        name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME))
                            ?: null,
                        type = cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE)).toInt(),
                        date = simpDate.format(
                            Date(
                                cursor.getLong(
                                    cursor.getColumnIndex(
                                        CallLog.Calls.DATE
                                    )
                                )
                            )
                        ).toString(),
                        duration = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION))
                            .toString(),
                        subscriberId = cursor.getString(cursor.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID))
                            ?: "",
                        calType = dir.toString(),
                        photouri = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_PHOTO_URI))
                            ?: "",
                        SimName = getSimCardInfosBySubscriptionId(
                            cursor.getString(cursor.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID))
                                ?: "0",
                        )?.displayName?.toString() ?: "",
                    )
                }?.let {
                    callHistoryList.add(
                        it
                    )
                }


        }
        cursor?.close()
        return callHistoryList

    }

    private fun getSimCardInfosBySubscriptionId(subscriptionId: String): SubscriptionInfo? {
        val subscriptionManager: SubscriptionManager =
            context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        return subscriptionManager.activeSubscriptionInfoList.find {
            try {
                it.subscriptionId == subscriptionId.toInt()
            } catch (e: Exception) {
                return null
            }
        }
    }

}