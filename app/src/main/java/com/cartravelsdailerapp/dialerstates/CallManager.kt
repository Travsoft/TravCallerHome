package com.cartravelsdailerapp.dialerstates

import android.database.Observable
import android.os.Build
import android.telecom.Call
import android.util.Log
import androidx.annotation.RequiresApi
import io.reactivex.subjects.BehaviorSubject

object CallManager {

    private const val LOG_TAG = "CallManager"

    private val subject = BehaviorSubject.create<GsmCall>()

    private var currentCall: Call? = null

    fun updates(): BehaviorSubject<GsmCall> = subject

    fun updateCall(call: Call?) {
        currentCall = call
        call?.let {
            subject.onNext(it.toGsmCall())
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun cancelCall() {
        currentCall?.let {
            when (it.state) {
                Call.STATE_RINGING -> rejectCall()
                else -> disconnectCall()

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun acceptCall() {

        Log.i(LOG_TAG, "acceptCall")
        currentCall?.let {
            it.answer(it.details.videoState)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun rejectCall() {
        Log.i(LOG_TAG, "rejectCall")
        currentCall?.reject(false, "")
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun disconnectCall() {
        Log.i(LOG_TAG, "disconnectCall")
        currentCall?.disconnect()
    }
}