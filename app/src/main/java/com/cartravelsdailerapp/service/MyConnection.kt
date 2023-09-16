package com.cartravelsdailerapp.service

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telecom.CallAudioState
import android.telecom.Connection
import androidx.annotation.RequiresApi
import io.reactivex.annotations.Nullable


@RequiresApi(Build.VERSION_CODES.M)
class MyConnection : Connection() {


    override fun onCallAudioStateChanged(state: CallAudioState?) {
        super.onCallAudioStateChanged(state)
    }

    override fun onStateChanged(state: Int) {
        super.onStateChanged(state)
    }

    override fun onPlayDtmfTone(c: Char) {
        super.onPlayDtmfTone(c)
    }

    override fun onStopDtmfTone() {
        super.onStopDtmfTone()
    }

    override fun onDisconnect() {
        super.onDisconnect()
    }

    override fun onSeparate() {
        super.onSeparate()
    }

    override fun onAbort() {
        super.onAbort()
    }

    override fun onHold() {
        super.onHold()
    }

    override fun onUnhold() {
        super.onUnhold()
    }

    override fun onAnswer(videoState: Int) {
        super.onAnswer(videoState)
    }

    override fun onAnswer() {
        super.onAnswer()
    }

    override fun onDeflect(address: Uri?) {
        super.onDeflect(address)
    }

    override fun onReject() {
        super.onReject()
    }

    override fun onReject(replyMessage: String?) {
        super.onReject(replyMessage)
    }

    override fun onPostDialContinue(proceed: Boolean) {
        super.onPostDialContinue(proceed)
    }

    override fun onPullExternalCall() {
        super.onPullExternalCall()
    }

    override fun onCallEvent(event: String?, extras: Bundle?) {
        super.onCallEvent(event, extras)
    }

    override fun onHandoverComplete() {
        super.onHandoverComplete()
    }

    override fun onExtrasChanged(extras: Bundle?) {
        super.onExtrasChanged(extras)
    }

    override fun onShowIncomingCallUi() {
        super.onShowIncomingCallUi()
    }


    override fun onStopRtt() {
        super.onStopRtt()
    }

    override fun handleRttUpgradeResponse(@Nullable rttTextStream: RttTextStream?) {
        super.handleRttUpgradeResponse(rttTextStream)
    }

    override fun sendConnectionEvent(event: String?, extras: Bundle?) {
        super.sendConnectionEvent(event, extras)
    }
}