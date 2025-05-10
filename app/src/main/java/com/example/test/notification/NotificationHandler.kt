package com.example.test.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.graphics.Color


object NotificationHandler {
    private const val CHANNEL_ID = "default_channel"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Default Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Simple Notifications"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showSimpleNotification(context: Context) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Simple Notification")
            .setContentText("This is a test notification.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            notify(1, builder.build())
        }
    }

    fun showFireDetectionNotification(context: Context) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("🔥 Fire Detected!")
            .setContentText("A fire has been detected by the sensors.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(Color.RED)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(2, builder.build())
        }
    }

    fun showGasDetectionNotification(context: Context) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("⚠️ Gas Leak Detected!")
            .setContentText("Gas sensors have detected a possible leak.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(Color.YELLOW)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(3, builder.build())
        }
    }

    fun showEarthquakeDetectionNotification(context: Context) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("🌍 Earthquake Detected!")
            .setContentText("Seismic activity detected. Take precautionary measures.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(Color.MAGENTA)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(4, builder.build())
        }
    }

    fun showSecurityDetectionNotification(context: Context) {
        val yesIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = "com.example.test.ACTION_YES"
        }
        val yesPendingIntent = PendingIntent.getBroadcast(
            context, 0, yesIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val noIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = "com.example.test.ACTION_NO"
        }
        val noPendingIntent = PendingIntent.getBroadcast(
            context, 1, noIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("🚨 Motion Detected!")
            .setContentText("Person detected outside your door. Open the door?")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(Color.BLUE)
            .addAction(android.R.drawable.ic_menu_view, "Yes", yesPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "No", noPendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(5, builder.build())
        }
    }
}