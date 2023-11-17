package com.cartravelsdailerapp.broadcastreceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.CancellationSignal
import android.provider.CallLog
import android.provider.ContactsContract
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.cartravelsdailerapp.PrefUtils
import com.cartravelsdailerapp.PrefUtils.LOCAL_BROADCAST_KEY


class CallEndReceiver : BroadcastReceiver() {
    var c: Context? = null

    override fun onReceive(context: Context, intent: Intent?) {
        try {
            println("Receiver start")
            val state = intent!!.getStringExtra(TelephonyManager.EXTRA_STATE)
            val incomingNumber = intent!!.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            Log.e("Incoming Number", "Number is ,$incomingNumber")
            if (incomingNumber != null) {
                val (name, photoUri) = getCallerInfo(context, incomingNumber)
                val intent = Intent(LOCAL_BROADCAST_KEY)
                intent.putExtra(PrefUtils.ContactNumber, incomingNumber)
                intent.putExtra(PrefUtils.PhotoUri, photoUri)
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
            }
            Log.e("State", "State is ,$state")
            if (state == TelephonyManager.EXTRA_STATE_RINGING) {
                Toast.makeText(context, "Incoming Call State", Toast.LENGTH_SHORT).show()
                Toast.makeText(
                    context,
                    "Ringing State Number is -$incomingNumber",
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