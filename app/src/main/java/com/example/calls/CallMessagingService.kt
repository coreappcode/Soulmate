package com.example.calls

import android.util.Log
import com.example.data.ChatPolicies
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class CallMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            CallPushTokenStore.register(this, uid, token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        when (message.data["type"]) {
            "incoming_message" -> {
                val conversationId = message.data["conversationId"]
                val currentUid = FirebaseAuth.getInstance().currentUser?.uid
                val recipientId = message.data["recipientId"]
                if (conversationId.isNullOrBlank() ||
                    !ChatPolicies.isNotificationForUser(currentUid, recipientId)
                ) {
                    Log.w("CallMessagingService", "Ignoring a chat notification for a different or signed-out user.")
                    return
                }
                ChatNotificationManager.showMessage(
                    context = this,
                    conversationId = conversationId,
                    senderName = message.data["senderName"].orEmpty(),
                    body = message.data["body"].orEmpty()
                )
            }

            "incoming_call" -> showIncomingCall(message)
            else -> Unit
        }
    }

    private fun showIncomingCall(message: RemoteMessage) {
        val callId = message.data["callId"]
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid
        if (callId.isNullOrBlank() || currentUid == null ||
            message.data["calleeId"] != currentUid
        ) {
            Log.w("CallMessagingService", "Ignoring an incoming call for a different or signed-out user.")
            return
        }

        CallNotificationManager.showIncomingCall(
            context = this,
            callId = callId,
            callType = message.data["callType"].orEmpty(),
            callerName = message.data["callerName"].orEmpty(),
            callerPhoto = message.data["callerPhoto"].orEmpty()
        )
    }
}
