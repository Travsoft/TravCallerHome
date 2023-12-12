package com.cartravelsdailerapp.broadcastreceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.provider.CallLog
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.cartravelsdailerapp.PrefUtils
import com.cartravelsdailerapp.PrefUtils.LOCAL_BROADCAST_KEY
import com.cartravelsdailerapp.models.CallHistory
import com.cartravelsdailerapp.ui.CallActivity
import java.util.*


class CallEndReceiver : BroadcastReceiver() {
    var c: Context? = null
    override fun onReceive(context: Context, intent: Intent?) {
        try {
            c = context
            var phoneNumer = ""
            val a = intent?.action
            if (a == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
                phoneNumer = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER) ?: ""
            } else if (a == Intent.ACTION_NEW_OUTGOING_CALL) {

                phoneNumer = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER) ?: ""
            }

            if (phoneNumer.isBlank())
                return
            val callLogsByNumber = getCallLogsByNumber(phoneNumer)

            val intent = Intent(LOCAL_BROADCAST_KEY)
            intent.putExtra(PrefUtils.ContactNumber, phoneNumer)
            intent.putExtra(PrefUtils.PhotoUri, callLogsByNumber.photouri)
            intent.putExtra(PrefUtils.SIMIndex, callLogsByNumber.SimName)
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        //  callActionHandler.postDelayed(runRingingActivity, 0);

    }

    var callActionHandler: Handler = Handler()
    var runRingingActivity = Runnable {
        val intentPhoneCall = Intent(c, CallActivity::class.java)
        intentPhoneCall.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intentPhoneCall.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        c!!.startActivity(intentPhoneCall)
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