package com.cartravelsdailerapp.broadcastreceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
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
import com.cartravelsdailerapp.models.CallHistory

class CustomPhoneStateReceiver(
    private val onResult: (String, String?, Uri?, String) -> Unit,
    val number: String
) : BroadcastReceiver() {
    var c: Context? = null
    var simSlotIndex: Int = 0
    var simName: String = ""
    override fun onReceive(context: Context, intent: Intent?) {
        Toast.makeText(context,"CustomPhoneStateReceiver",Toast.LENGTH_SHORT).show()

        val state = intent?.getStringExtra(TelephonyManager.EXTRA_STATE)
        var phoneNumer = ""
        val a = intent?.action
        if (a == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            phoneNumer = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER) ?: ""
            if (phoneNumer.isNotBlank()){
                getCallLogsByNumber(phoneNumer)
            }
        }
        if (a == Intent.ACTION_NEW_OUTGOING_CALL) {
            phoneNumer = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER) ?: ""
            if (phoneNumer.isNotBlank())
            {
                getCallLogsByNumber(phoneNumer)
            }
        }
        if (phoneNumer.isBlank()){
            return
        }
        Log.e("e 28", phoneNumer.toString())
        val subscriptionManager: SubscriptionManager =
            context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

        val subscriptionInfoList: List<SubscriptionInfo> =
            subscriptionManager.getActiveSubscriptionInfoList()
        if (subscriptionInfoList != null) {
            // Loop through the list of active subscriptions
            for (subscriptionInfo in subscriptionInfoList) {
                // Retrieve the phone number of the SIM card
                if (phoneNumer != null && subscriptionInfo.number.equals(phoneNumer)) {
                    // The incoming call belongs to this SIM card
                    simSlotIndex = subscriptionInfo.simSlotIndex
                    simName = subscriptionInfo.displayName.toString()

                    break
                }
            }
        }
        if (state == TelephonyManager.EXTRA_STATE_RINGING || state == TelephonyManager.EXTRA_STATE_OFFHOOK) {
            phoneNumer?.let { number ->
                val callHistory = getCallerInfo(context, number)
                onResult(
                    number,
                    callHistory.name,
                    Uri.parse(callHistory.photouri),
                    simSlotIndex.toString()
                )
            }
        } else {
            val callHistory = getCallerInfo(context, number)
            onResult(
                number,
                callHistory.name,
                Uri.parse(callHistory.photouri),
                simSlotIndex.toString()
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
    fun getCallLogsByNumber(phoneNumber: String) {
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
                Log.e("call Logs -> ${phoneNumber}", "${id} ${number} ${name}")
                Log.d("call Logs -> ${phoneNumber}", "${id} ${number} ${name}")
            }
        }
    }


}