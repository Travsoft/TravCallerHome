package com.cartravelsdailerapp.broadcastreceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
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


class CallEndReceiver : BroadcastReceiver() {
    var c: Context? = null
    var simSlotIndex: Int = 0
    var simName: String = ""
    override fun onReceive(context: Context, intent: Intent?) {
        try {
            c = context
            val state = intent!!.getStringExtra(TelephonyManager.EXTRA_STATE)
            var phoneNumer = ""
            val a = intent?.action
            if (a == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
                phoneNumer = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER) ?: ""
            }
            if (a == Intent.ACTION_NEW_OUTGOING_CALL) {
                phoneNumer = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER) ?: ""
            }
            if (phoneNumer.isBlank())
                return
            val subscriptionManager: SubscriptionManager =
                context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

            val subscriptionInfoList: List<SubscriptionInfo> =
                subscriptionManager.getActiveSubscriptionInfoList()
            for (subscriptionInfo in subscriptionInfoList) {
                // Retrieve the phone number of the SIM card
                if (subscriptionInfo.number.equals(phoneNumer)) {
                    // The incoming call belongs to this SIM card
                    simSlotIndex = subscriptionInfo.simSlotIndex
                    simName = subscriptionInfo.displayName.toString()

                    Log.d(
                        "52",
                        "Incoming call belongs to SIM card in slot $simSlotIndex $simName"
                    )
                    break
                }
            }
            val callHistory = getCallerInfo(context, phoneNumer)
            val intent = Intent(LOCAL_BROADCAST_KEY)
            intent.putExtra(PrefUtils.ContactNumber, phoneNumer)
            intent.putExtra(PrefUtils.PhotoUri, callHistory.photouri)
            intent.putExtra(PrefUtils.SIMIndex, callHistory.SimName.toInt())
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
            Log.e("State", "State is ,$state")
            Log.e("Sim Index", "State is ,${callHistory.SimName}")
            if (state == TelephonyManager.EXTRA_STATE_RINGING) {
                Toast.makeText(
                    context,
                    "Ringing State Number is -$phoneNumer",
                    Toast.LENGTH_SHORT
                ).show()
            }
            if (state == TelephonyManager.EXTRA_STATE_OFFHOOK) {
                Toast.makeText(context, "Call Received State", Toast.LENGTH_SHORT).show()
            }
            if (state == TelephonyManager.EXTRA_STATE_IDLE) {
                Toast.makeText(context, "Call Idle State", Toast.LENGTH_SHORT).show()
            }

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

    @RequiresApi(Build.VERSION_CODES.M)
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


}