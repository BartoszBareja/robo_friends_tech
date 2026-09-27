package com.example.robofriendstech

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

private const val TAG = "RoboFriendFcm"

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(TAG, "Message received from ${message.from}: ${message.notification?.body}")
        // TODO: show a local notification / update app state with the message payload.
    }

    override fun onRegistered(fid: String) {
        Log.d(TAG, "Registered with Firebase Installation ID: $fid")
        // TODO: send this FID to your backend so it can target pushes at this device.
    }
}
