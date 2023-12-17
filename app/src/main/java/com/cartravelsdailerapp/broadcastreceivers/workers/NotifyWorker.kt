package com.cartravelsdailerapp.broadcastreceivers.workers

import android.content.Context
import android.database.Cursor
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.provider.CallLog
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cartravelsdailerapp.PrefUtils
import com.cartravelsdailerapp.db.DatabaseBuilder
import com.cartravelsdailerapp.models.CallHistory
import java.util.*

class NotifyWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result  {
        val number = inputData.getString(PrefUtils.ContactNumber)
        val simName = inputData.getString(PrefUtils.SIMIndex)
        var db = DatabaseBuilder.getInstance(applicationContext).CallHistoryDao()
        val callLogs = db.getAllCallLogs()

        val callHistory = callLogs.find {
            it.number == number
        }
        if (callHistory == null) {
            val callhistoryData = getCallLogsByNumber(applicationContext, number!!)
            if (!callhistoryData.number.isNullOrBlank()){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val simpDate = SimpleDateFormat(PrefUtils.DataFormate)
                    val date = Date()
                    val current = simpDate.format(date)
                    var name = ""
                    if (callhistoryData.name == null) {
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
                            simName!!
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
                            simName!!
                        )
                    )
                }

            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val simpleDateFormat = SimpleDateFormat(PrefUtils.DataFormate)
                val date = simpleDateFormat.format(Date())
                db.updateCallHistory(
                    date, simName!!, callHistory.id
                )
            } else {
                val d = Date()
                db.updateCallHistory(
                    d.toString(), simName!!, callHistory.id
                )
            }
        }
       return Result.success()
    }
    private fun getCallLogsByNumber(context:Context,phoneNumber: String): CallHistory {
        var typeDisplayName = ""
        var date: String
        val cursor: Cursor? = context?.contentResolver?.query(
            CallLog.Calls.CONTENT_URI,
            null,
            CallLog.Calls.NUMBER + " = ? ",
            arrayOf(phoneNumber),
            ""
        )

        if (cursor?.moveToFirst() == true) {
            while (cursor.moveToNext()) {
                val id = cursor.getString(cursor.getColumnIndex(CallLog.Calls._ID))
                val number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER))
                val name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME))
                val type = cursor.getString(
                    cursor.getColumnIndex(CallLog.Calls.TYPE)
                )
                when (type.toInt()) {
                    CallLog.Calls.OUTGOING_TYPE -> typeDisplayName = "OUTGOING"
                    CallLog.Calls.INCOMING_TYPE -> typeDisplayName = "INCOMING"
                    CallLog.Calls.MISSED_TYPE -> typeDisplayName = "MISSED"
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val simpDate = SimpleDateFormat(PrefUtils.DataFormate)
                    date = simpDate.format(
                        Date(
                            cursor.getLong(
                                cursor.getColumnIndex(
                                    CallLog.Calls.DATE
                                )
                            )
                        )
                    ).toString()

                } else {
                    date = Date(
                        cursor.getLong(
                            cursor.getColumnIndex(
                                CallLog.Calls.DATE
                            )
                        )
                    ).toString()
                }
                val duration = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION))
                val subscriberId =
                    cursor.getString(cursor.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID))
                        ?: ""
                val photouri =
                    cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_PHOTO_URI))
                        ?: ""
                val simName = getSimCardInfosBySubscriptionId(context,
                    cursor.getString(cursor.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID))
                        ?: "0",
                )?.displayName?.toString() ?: ""
                val calLogs = CallHistory(
                    typeDisplayName,
                    number,
                    name,
                    type.toInt(),
                    date,
                    duration.toString(),
                    subscriberId,
                    photouri,
                    simName
                )
                return calLogs

            }
        }
        return CallHistory(
            "",
            "",
            "",
            0,
            "",
            "",
            "",
            "",
            ""
        )

    }
    private fun getSimCardInfosBySubscriptionId(context: Context,subscriptionId: String): SubscriptionInfo? {
        val subscriptionManager: SubscriptionManager =
            context?.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        return subscriptionManager.activeSubscriptionInfoList.find {
            try {
                it.subscriptionId == subscriptionId.toInt()
            } catch (e: Exception) {
                return null
            }
        }
    }

}