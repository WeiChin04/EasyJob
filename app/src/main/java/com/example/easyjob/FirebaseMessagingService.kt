package com.example.easyjob

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Save the device token to the Realtime Database
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        FirebaseDatabase.getInstance().reference
            .child("Users")
            .child(userId)
            .child("deviceToken")
            .setValue(token)
    }
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        // Handle incoming FCM messages
        // ...
    }

}