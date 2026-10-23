package com.example.calls

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.R

object CallNotificationManager {
    private const val CHANNEL_ID = "incoming_calls"

    fun showIncomingCall(
        context: Context,
        callId: String,
        callType: String,
        callerName: String,
        callerPhoto: String
    ) {
        val manager = context.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Incoming calls",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Alerts for incoming Soulmate audio and video calls"
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PRIVATE
                }
            )
        }

        val contentIntent = callIntent(context, callId, callType, callerName, callerPhoto)
        val answerIntent = callIntent(context, callId, callType, callerName, callerPhoto)
            .putExtra(CallActivity.EXTRA_AUTO_ANSWER, true)
        val declineIntent = callIntent(context, callId, callType, callerName, callerPhoto)
            .putExtra(CallActivity.EXTRA_AUTO_DECLINE, true)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_call_notification)
            .setContentTitle(callerName.ifBlank { "Incoming call" })
            .setContentText(if (callType == "video") "Incoming video call" else "Incoming audio call")
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(pendingActivity(context, contentIntent, callId.hashCode()))
            .setFullScreenIntent(
                pendingActivity(context, contentIntent, callId.hashCode() + 3),
                true
            )
            .addAction(
                R.drawable.ic_call_notification,
                "Decline",
                pendingActivity(context, declineIntent, callId.hashCode() + 1)
            )
            .addAction(
                R.drawable.ic_call_notification,
                "Answer",
                pendingActivity(context, answerIntent, callId.hashCode() + 2)
            )
            .build()

        manager.notify(callId.hashCode(), notification)
    }

    fun dismiss(context: Context, callId: String) {
        context.getSystemService(NotificationManager::class.java).cancel(callId.hashCode())
    }

    private fun callIntent(
        context: Context,
        callId: String,
        callType: String,
        callerName: String,
        callerPhoto: String
    ): Intent = Intent(context, CallActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra(CallActivity.EXTRA_CALL_ID, callId)
        putExtra(CallActivity.EXTRA_CALL_TYPE, callType)
        putExtra(CallActivity.EXTRA_CALLER_NAME, callerName)
        putExtra(CallActivity.EXTRA_CALLER_PHOTO, callerPhoto)
        putExtra(CallActivity.EXTRA_INCOMING, true)
    }

    private fun pendingActivity(context: Context, intent: Intent, requestCode: Int): PendingIntent =
        PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
}
