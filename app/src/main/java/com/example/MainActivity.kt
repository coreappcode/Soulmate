package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import com.example.navigation.SoulmateNavGraph
import com.example.ui.theme.SoulmateTheme
import com.example.calls.CallPushTokenStore
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {
    private val notificationConversationId = MutableStateFlow<String?>(null)

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        if (auth.currentUser != null) {
            CallPushTokenStore.registerCurrentUser(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationConversationId.value = intent.getStringExtra(EXTRA_OPEN_CONVERSATION_ID)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContent {
            val conversationId by notificationConversationId.collectAsState()
            SoulmateTheme {
                SoulmateNavGraph(
                    notificationConversationId = conversationId,
                    onNotificationConversationHandled = { handledId ->
                        if (notificationConversationId.value == handledId) {
                            notificationConversationId.value = null
                        }
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        notificationConversationId.value = intent.getStringExtra(EXTRA_OPEN_CONVERSATION_ID)
    }

    override fun onStart() {
        super.onStart()
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener)
    }

    override fun onStop() {
        FirebaseAuth.getInstance().removeAuthStateListener(authStateListener)
        super.onStop()
    }

    companion object {
        const val EXTRA_OPEN_CONVERSATION_ID = "open_conversation_id"
    }
}
