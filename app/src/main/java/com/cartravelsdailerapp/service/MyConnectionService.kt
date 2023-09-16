package com.cartravelsdailerapp.service

import android.content.Intent
import android.os.Build
import android.telecom.*
import androidx.annotation.RequiresApi


@RequiresApi(Build.VERSION_CODES.M)
class MyConnectionService : ConnectionService() {
    override fun onUnbind(intent: Intent): Boolean {
        return super.onUnbind(intent)
    }

    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle,
        request: ConnectionRequest
    ): Connection {
        return super.onCreateIncomingConnection(connectionManagerPhoneAccount, request)
    }

    override fun onCreateIncomingConnectionFailed(
        connectionManagerPhoneAccount: PhoneAccountHandle,
        request: ConnectionRequest
    ) {
        super.onCreateIncomingConnectionFailed(connectionManagerPhoneAccount, request)
    }

    override fun onCreateOutgoingConnectionFailed(
        connectionManagerPhoneAccount: PhoneAccountHandle,
        request: ConnectionRequest
    ) {
        super.onCreateOutgoingConnectionFailed(connectionManagerPhoneAccount, request)
    }

    override fun onCreateOutgoingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle,
        request: ConnectionRequest
    ): Connection {
        return super.onCreateOutgoingConnection(connectionManagerPhoneAccount, request)
    }

    override fun onCreateOutgoingHandoverConnection(
        fromPhoneAccountHandle: PhoneAccountHandle,
        request: ConnectionRequest
    ): Connection {
        return super.onCreateOutgoingHandoverConnection(fromPhoneAccountHandle, request)
    }

    override fun onCreateIncomingHandoverConnection(
        fromPhoneAccountHandle: PhoneAccountHandle,
        request: ConnectionRequest
    ): Connection {
        return super.onCreateIncomingHandoverConnection(fromPhoneAccountHandle, request)
    }

    override fun onHandoverFailed(request: ConnectionRequest, error: Int) {
        super.onHandoverFailed(request, error)
    }

    override fun onConference(connection1: Connection?, connection2: Connection?) {
        super.onConference(connection1, connection2)
    }

    override fun onRemoteConferenceAdded(conference: RemoteConference) {
        super.onRemoteConferenceAdded(conference)
    }

    override fun onRemoteExistingConnectionAdded(connection: RemoteConnection) {
        super.onRemoteExistingConnectionAdded(connection)
    }

    override fun onConnectionServiceFocusLost() {
        super.onConnectionServiceFocusLost()
    }

    override fun onConnectionServiceFocusGained() {
        super.onConnectionServiceFocusGained()
    }
}