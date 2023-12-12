package com.cartravelsdailerapp.broadcastreceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.os.CancellationSignal
import android.provider.CallLog
import android.provider.ContactsContract
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import com.cartravelsdailerapp.PrefUtils
import com.cartravelsdailerapp.models.CallHistory
import java.util.*

class CustomPhoneStateReceiver(
    private val onResult: (String, String?, Uri?, String) -> Unit,
    val number: String
) : BroadcastReceiver() {
    var c: Context? = null

    override fun onReceive(context: Context, intent: Intent?) {
        var phoneNumer = ""
        val a = intent?.action
        if (a == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            phoneNumer = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER) ?: ""
        }
        if (a == Intent.ACTION_NEW_OUTGOING_CALL) {
            phoneNumer = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER) ?: ""
        }
        if (phoneNumer.isBlank()) {
            return
        }

        val callLogsByNumber = getCallLogsByNumber(phoneNumer)

        phoneNumer?.let { number ->
            val callHistory = getCallerInfo(context, number)
            onResult(
                phoneNumer,
                callLogsByNumber.name,
                Uri.parse(callLogsByNumber.photouri),
                callLogsByNumber.SimName
            )
        }


    }

    private fun getCallerInfo(context: Context, phoneNumber: String): CallHistory {
        val cursor: Cursor? = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            null,
            CallLog.Calls.NUMBER + " = ? OR " + CallLog.Calls.NUMBER + " = ?",
            arrayOf(phoneNumber),
            CallLog.Calls.DATE + " DESC"
        )
        if (cursor != null && cursor.moveToFirst()) {
            val indexUri = cursor.getColumnIndex(CallLog.Calls.CACHED_PHOTO_URI)
            val indexName = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val indexType = cursor.getColumnIndex(CallLog.Calls.TYPE)
            val simIdColumnIndex = cursor.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID)
            val accountId: String = cursor.getString(simIdColumnIndex)
            val simCardSlotIndex =
                getSimSlotIndexFromAccountId(context.applicationContext, accountId)
            val name = cursor.getString(indexName)
            val type = cursor.getType(indexType)

            val photoUri =
                cursor.getString(indexUri)?.let { Uri.parse(it) }

            return CallHistory(
                "",
                phoneNumber,
                name,
                type,
                "",
                "",
                "",
                photoUri.toString(),
                simCardSlotIndex.toString()
            )
        }
        return CallHistory(
            "", phoneNumber, "", 0, "", "", "", "", ""
        )
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun getSimSlotIndexFromAccountId(context: Context, accountIdToFind: String): Int {
        // This is actually the official data that should be found, as on the emulator, but sadly not all phones return here a proper value
        val telecomManager = context.getSystemService<TelecomManager>()
        telecomManager?.callCapablePhoneAccounts?.forEachIndexed { index, account: PhoneAccountHandle ->
            val phoneAccount: PhoneAccount = telecomManager?.getPhoneAccount(account)!!
            val accountId: String = phoneAccount.accountHandle
                .id
            if (accountIdToFind == accountId) {
                return index
            }
        }
        accountIdToFind.toIntOrNull()?.let {
            if (it >= 0)
                return it
        }
        return -1
    }

    private fun getCallLogsByNumber(phoneNumber: String): CallHistory {
        var typeDisplayName = ""
        var date: String
        val cursor: Cursor? = c?.contentResolver?.query(
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
                val simName = getSimCardInfosBySubscriptionId(
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

    private fun getSimCardInfosBySubscriptionId(subscriptionId: String): SubscriptionInfo? {
        val subscriptionManager: SubscriptionManager =
            c?.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        return subscriptionManager.activeSubscriptionInfoList.find {
            try {
                it.subscriptionId == subscriptionId.toInt()
            } catch (e: Exception) {
                return null
            }
        }
    }


}