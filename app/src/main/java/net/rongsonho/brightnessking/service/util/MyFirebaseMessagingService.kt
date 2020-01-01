package net.rongsonho.brightnessking.service.util

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.lang.Exception

private const val TAG = "FCMService"

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "onMessageReceived: $remoteMessage")
    }

    override fun onMessageSent(msg: String) {
        Log.d(TAG, "onMessageSent: $msg")
    }

    override fun onDeletedMessages() {
        Log.d(TAG, "onDeletedMessages")
    }

    override fun onSendError(p0: String, p1: Exception) {
        Log.d(TAG, "onSendError, p0: $p0, p1: $p1")
    }

    override fun onNewToken(p0: String) {
        Log.d(TAG, "onNewToken, p0: $p0")
    }
}