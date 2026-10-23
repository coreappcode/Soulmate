package com.example.calls

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.R

class CallForegroundService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val callId = intent?.getStringExtra(CallActivity.EXTRA_CALL_ID)
        if (callId.isNullOrBlank()) {
            stopSelf(startId)
            return START_NOT_STICKY
        }

        val callType = intent.getStringExtra(CallActivity.EXTRA_CALL_TYPE).orEmpty()
        val displayName = intent.getStringExtra(CallActivity.EXTRA_CALLER_NAME).orEmpty()
        createNotificationChannel()
        val notification = createNotification(callId, callType, displayName)
        val foregroundType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE or
                if (callType == "video") ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA else 0
        } else {
            0
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, foregroundType)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Active calls",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps an active call connected while Soulmate is in the background"
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun createNotification(callId: String, callType: String, displayName: String): Notification {
        val intent = Intent(this, CallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(CallActivity.EXTRA_CALL_ID, callId)
            putExtra(CallActivity.EXTRA_CALL_TYPE, callType)
            putExtra(CallActivity.EXTRA_CALLER_NAME, displayName)
            putExtra(CallActivity.EXTRA_INCOMING, false)
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            this,
            callId.hashCode(),
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_call_notification)
            .setContentTitle("Soulmate call")
            .setContentText(if (displayName.isBlank()) "Call in progress" else "Call with $displayName")
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "active_calls"
        private const val NOTIFICATION_ID = 7201
    }
}
