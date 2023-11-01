package com.cartravelsdailerapp.broadcastreceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.CancellationSignal
import android.provider.CallLog
import android.provider.ContactsContract
import android.telephony.TelephonyManager
import android.widget.Toast

class CustomPhoneStateReceiver(private val onResult: (String, String?, Uri?) -> Unit) :
    BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        println("CustomPhoneStateReceiver onReceive")

        val state = intent?.getStringExtra(TelephonyManager.EXTRA_STATE)
        val incomingNumber = intent?.getStringExtra("incoming_number")
        Toast.makeText(context, state, Toast.LENGTH_SHORT).show()
        if (state == TelephonyManager.EXTRA_STATE_RINGING || state == TelephonyManager.EXTRA_STATE_OFFHOOK) {
            println("TelephonyManager.CALL_STATE_RINGING onReceive -> $incomingNumber")
            incomingNumber?.let { number ->
                val (name, photoUri) = getCallerInfo(context, number)
                onResult(number, name, photoUri)
            }
        }
    }

    private fun getCallerInfo(context: Context, phoneNumber: String): Pair<String?, Uri?> {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
        val projection = arrayOf(
            ContactsContract.PhoneLookup.DISPLAY_NAME,
            ContactsContract.PhoneLookup.PHOTO_URI
        )

        context.contentResolver.query(uri, projection, null, null, null).use { cursor ->
            if (cursor?.moveToFirst() == true) {
                val nameIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                val photoIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_URI)

                val name = cursor.getString(nameIndex)
                val photoUri = cursor.getString(photoIndex)?.let { Uri.parse(it) }
                return Pair(name, photoUri)
            }
        }
        val cancellationSignal = CancellationSignal()
        val cursor = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            null,
            CallLog.Calls.TYPE + " = ?",
            arrayOf(CallLog.Calls.INCOMING_TYPE.toString()),
            CallLog.Calls.DATE + " DESC",
            cancellationSignal
        )
        if (cursor != null && cursor.moveToFirst()) {
            val indexName = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val name = cursor.getString(if (indexName < 0) 0 else indexName)
            println(indexName)
            val indexUri = cursor.getColumnIndex(CallLog.Calls.CACHED_PHOTO_URI)
            val photoUri =
                cursor.getString(if (indexUri < 0) 0 else indexUri)?.let { Uri.parse(it) }

            cancellationSignal.cancel()
            cursor.close()

            return Pair(name, photoUri)
        }


        return Pair(null, null)
    }
}