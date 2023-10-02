package com.cartravelsdailerapp.broadcastreceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.cartravelsdailerapp.PrefUtils.LOCAL_BROADCAST_KEY


class CallEndReceiver : BroadcastReceiver() {
    var c: Context? = null

    override fun onReceive(context: Context, intent: Intent?) {
        c = context
        try {
            val tmgr = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val PhoneListener = intent?.let { MyPhoneStateListener(c!!, it) }
            tmgr.listen(PhoneListener, PhoneStateListener.LISTEN_CALL_STATE)
        } catch (e: Exception) {
            Toast.makeText(context, "oops!", Toast.LENGTH_SHORT).show()
        }
    }

    class MyPhoneStateListener(var context: Context,var i: Intent) : PhoneStateListener() {

        override fun onCallStateChanged(state: Int, incomingNumber: String) {
            if (state == 0) {
/*
                startActivity(
                    context,
                    Intent(context, MainActivity::class.java)
                        .setFlags(FLAG_ACTIVITY_NEW_TASK)
                        .addFlags(FLAG_ACTIVITY_CLEAR_TOP),
                    null
                )
*/
                val intent = Intent(LOCAL_BROADCAST_KEY)
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
            }
        }
    }
}