package com.cartravelsdailerapp.broadcastreceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.cartravelsdailerapp.MainActivity
import com.google.android.material.snackbar.Snackbar


class CallEndReceiver : BroadcastReceiver() {
    var c: Context? = null

    override fun onReceive(context: Context, intent: Intent?) {
        c = context
        try {
            val tmgr = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val PhoneListener = MyPhoneStateListener(c!!)
            tmgr.listen(PhoneListener, PhoneStateListener.LISTEN_CALL_STATE)
        } catch (e: Exception) {
            Toast.makeText(context, "oops!", Toast.LENGTH_SHORT).show()
        }
    }

    class MyPhoneStateListener(var context: Context) : PhoneStateListener() {

        override fun onCallStateChanged(state: Int, incomingNumber: String) {
            if (state == 0) {
                startActivity(
                    context,
                    Intent(context, MainActivity::class.java).setFlags(FLAG_ACTIVITY_NEW_TASK),
                    null
                )
            }
        }
    }
}