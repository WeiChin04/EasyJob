package com.example.easyjob

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Vibrator
import android.util.Log
import android.window.SplashScreen
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*

class FirebaseMessagingService : com.google.firebase.messaging.FirebaseMessagingService(){

    private val ADMIN_CHANNEL_ID = "admin_channel"
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var dbUser: DatabaseReference
    private lateinit var dbEmployer: DatabaseReference

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        dbUser = db.getReference("Users")
        dbEmployer = db.getReference("Employers")


        if(auth.currentUser != null) {
            dbUser.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChild(auth.currentUser!!.uid)) {
                        val userId = FirebaseAuth.getInstance().currentUser!!.uid
                        FirebaseDatabase.getInstance().reference
                            .child("Users")
                            .child(userId)
                            .child("deviceToken")
                            .setValue(token)
                    }
                }

                override fun onCancelled(error: DatabaseError) { Log.d("user","This is a Employer")}
            })
            dbEmployer.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChild(auth.currentUser!!.uid)) {
                        val employerId = FirebaseAuth.getInstance().currentUser!!.uid
                        FirebaseDatabase.getInstance().reference
                            .child("Employers")
                            .child(employerId)
                            .child("deviceToken")
                            .setValue(token)
                    }
                }

                override fun onCancelled(error: DatabaseError) {Log.d("employer","This is a User")}
            })
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val intent = Intent(this, MainActivity::class.java)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = Random().nextInt(3000)

        /*
        Apps targeting SDK 26 or above (Android O) must implement notification channels and add their notifications to at least one of them.
        Therefore, confirm if the version is Oreo or higher, then setup notification channel
        */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupChannels(notificationManager)
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT)
        val notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(remoteMessage.data["title"])
            .setContentText(remoteMessage.data["message"])
            .setAutoCancel(true)
            .setSound(notificationSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(Notification.PRIORITY_HIGH)

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            notificationBuilder.color = resources.getColor(R.color.green)
//        }

        notificationManager.notify(notificationID, notificationBuilder.build())
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun setupChannels(notificationManager: NotificationManager) {
        val adminChannelName = "New notification"
        val adminChannelDescription = "Device to device notification "

        val adminChannel = NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_HIGH)

        adminChannel.description = adminChannelDescription
        adminChannel.enableLights(true)
        adminChannel.lightColor = Color.RED
        adminChannel.enableVibration(true)

        notificationManager.createNotificationChannel(adminChannel)
    }
}
//    @SuppressLint("UnspecifiedImmutableFlag")
//    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//        super.onMessageReceived(remoteMessage)
//
//        // playing audio and vibration when user se reques
//        val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
//        val r = RingtoneManager.getRingtone(applicationContext, notification)
//        r.play()
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            r.isLooping = false
//        }
//
//        // vibration
//        val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
//        val pattern = longArrayOf(100, 300, 300, 300)
//        v.vibrate(pattern, -1)
//
//        val resourceImage = resources.getIdentifier(remoteMessage.notification?.icon, "drawable", packageName)
//
//        val builder = NotificationCompat.Builder(this, "CHANNEL_ID")
//        builder.setSmallIcon(resourceImage)
//
//        val resultIntent = Intent(this, MainActivity::class.java)
//        val pendingIntent = PendingIntent.getActivity(this, 1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)
//
//        builder.setContentTitle(remoteMessage.notification?.title)
//        builder.setContentText(remoteMessage.notification?.body)
//        builder.setContentIntent(pendingIntent)
//        builder.setStyle(NotificationCompat.BigTextStyle().bigText(remoteMessage.notification?.body))
//        builder.setAutoCancel(true)
//        builder.setPriority(NotificationCompat.PRIORITY_MAX)
//
//        mNotificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channelId = "Your_channel_id"
//            val channel = NotificationChannel(channelId, "Channel human readable title", NotificationManager.IMPORTANCE_HIGH)
//            mNotificationManager?.createNotificationChannel(channel)
//            builder.setChannelId(channelId)
//        }
//
//        // notificationId is a unique int for each notification that you must define
//        mNotificationManager?.notify(100, builder.build())
//    }
//}
