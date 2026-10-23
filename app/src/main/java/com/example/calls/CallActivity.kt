package com.example.calls

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import com.example.ui.theme.SoulmateTheme

class CallActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            SoulmateTheme {
                CallScreen(
                    callId = intent.getStringExtra(EXTRA_CALL_ID),
                    calleeId = intent.getStringExtra(EXTRA_CALLEE_ID),
                    callType = intent.getStringExtra(EXTRA_CALL_TYPE) ?: "audio",
                    remoteName = intent.getStringExtra(EXTRA_CALLER_NAME).orEmpty(),
                    remotePhoto = intent.getStringExtra(EXTRA_CALLER_PHOTO).orEmpty(),
                    isIncoming = intent.getBooleanExtra(EXTRA_INCOMING, false),
                    autoAnswer = intent.getBooleanExtra(EXTRA_AUTO_ANSWER, false),
                    autoDecline = intent.getBooleanExtra(EXTRA_AUTO_DECLINE, false),
                    onFinish = ::finish
                )
            }
        }
    }

    companion object {
        const val EXTRA_CALL_ID = "call_id"
        const val EXTRA_CALLEE_ID = "callee_id"
        const val EXTRA_CALL_TYPE = "call_type"
        const val EXTRA_CALLER_NAME = "call_display_name"
        const val EXTRA_CALLER_PHOTO = "call_display_photo"
        const val EXTRA_INCOMING = "incoming_call"
        const val EXTRA_AUTO_ANSWER = "auto_answer_call"
        const val EXTRA_AUTO_DECLINE = "auto_decline_call"
    }
}
