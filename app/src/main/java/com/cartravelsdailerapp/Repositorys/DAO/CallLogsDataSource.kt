package com.cartravelsdailerapp.Repositorys.DAO

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.provider.CallLog
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import androidx.annotation.RequiresApi
import com.cartravelsdailerapp.PrefUtils
import com.cartravelsdailerapp.models.CallHistory
import java.util.*


class CallLogsDataSource(private val contentResolver: ContentResolver, val context: Context) {
    val callHistoryList = mutableListOf<CallHistory>()
    private lateinit var callHistory: CallHistory
    var dir: String? = null
    private lateinit var cursor: Cursor

    /*CallLog.Calls.DATE + " DESC"*/
    fun fetchCallLogsList(): List<CallHistory> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            cursor = contentResolver.query(
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
                null, CallLog.Calls.DATE + " DESC"
            )!!

        } else {
            cursor = contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(
                    CallLog.Calls.TYPE,
                    CallLog.Calls.NUMBER,
                    CallLog.Calls.CACHED_NAME,
                    CallLog.Calls.TYPE,
                    CallLog.Calls.DATE,
                    CallLog.Calls.DURATION,
                    CallLog.Calls.PHONE_ACCOUNT_ID
                ),
                null,
                null, CallLog.Calls.DATE + " DESC"
            )!!


        }
        while (cursor.moveToNext() == true) {
            when (cursor.getColumnIndex(CallLog.Calls.TYPE).let { cursor.getString(it).toInt() }) {
                CallLog.Calls.OUTGOING_TYPE -> dir = "OUTGOING"
                CallLog.Calls.INCOMING_TYPE -> dir = "INCOMING"
                CallLog.Calls.MISSED_TYPE -> dir = "MISSED"
            }
            cursor.getColumnIndex(CallLog.Calls.NUMBER)
                ?.let { cursor.getString(it) }?.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        val simpDate = SimpleDateFormat(PrefUtils.DataFormate)
                        CallHistory(
                            number = it,
                            name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME))
                                ?: null,
                            type = cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE))
                                .toInt(),
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

                    } else {
                        CallHistory(
                            number = it,
                            name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME))
                                ?: null,
                            type = cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE))
                                .toInt(),
                            date = Date(
                                cursor.getLong(
                                    cursor.getColumnIndex(
                                        CallLog.Calls.DATE
                                    )
                                )
                            ).toString(),
                            duration = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION))
                                .toString(),
                            subscriberId = cursor.getString(cursor.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID))
                                ?: "",
                            calType = dir.toString(),
                            photouri = "",
                            SimName = getSimCardInfosBySubscriptionId(
                                cursor.getString(cursor.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID))
                                    ?: "0",
                            )?.displayName?.toString() ?: "",
                        )
                    }

                }?.let {
                    callHistoryList.add(
                        it
                    )
                }
        }
        cursor.close()
        return callHistoryList
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun fetchCallLogSingle(): CallHistory {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            cursor = contentResolver.query(
                CallLog.Calls.CONTENT_URI.buildUpon()
                    .appendQueryParameter(CallLog.Calls.LIMIT_PARAM_KEY, "1")
                    .build(), arrayOf(
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
                null, CallLog.Calls.DATE + " DESC"
            )!!
        } else {
            cursor = contentResolver.query(
                CallLog.Calls.CONTENT_URI.buildUpon()
                    .appendQueryParameter(CallLog.Calls.LIMIT_PARAM_KEY, "1")
                    .build(), arrayOf(
                    CallLog.Calls.TYPE,
                    CallLog.Calls.NUMBER,
                    CallLog.Calls.CACHED_NAME,
                    CallLog.Calls.TYPE,
                    CallLog.Calls.DATE,
                    CallLog.Calls.DURATION,
                    CallLog.Calls.PHONE_ACCOUNT_ID
                ),
                null,
                null, CallLog.Calls.DATE + " DESC"
            )!!

        }
        while (cursor.moveToNext() == true) {
            when (cursor.getColumnIndex(CallLog.Calls.TYPE).let { cursor.getString(it).toInt() }) {
                CallLog.Calls.OUTGOING_TYPE -> dir = "OUTGOING"
                CallLog.Calls.INCOMING_TYPE -> dir = "INCOMING"
                CallLog.Calls.MISSED_TYPE -> dir = "MISSED"
            }
            cursor.getColumnIndex(CallLog.Calls.NUMBER)
                .let { cursor.getString(it) }?.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        val simpDate = SimpleDateFormat(PrefUtils.DataFormate)
                        callHistory = CallHistory(
                            number = it,
                            name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME))
                                ?: null,
                            type = cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE))
                                .toInt(),
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

                    } else {
                        callHistory = CallHistory(
                            number = it,
                            name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME))
                                ?: null,
                            type = cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE))
                                .toInt(),
                            date =
                            Date(
                                cursor.getLong(
                                    cursor.getColumnIndex(
                                        CallLog.Calls.DATE
                                    )
                                )
                            ).toString(),
                            duration = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION))
                                .toString(),
                            subscriberId = cursor.getString(cursor.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID))
                                ?: "",
                            calType = dir.toString(),
                            photouri = "",
                            SimName = getSimCardInfosBySubscriptionId(
                                cursor.getString(cursor.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID))
                                    ?: "0",
                            )?.displayName?.toString() ?: "",
                        )

                    }

                }
        }
        cursor.close()

        return callHistory

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