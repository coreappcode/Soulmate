package com.example.ui.screens.chats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.example.data.User
import com.example.ui.components.GradientButton
import com.example.ui.components.SoulTextField
import com.example.ui.components.SoulTopBar
import com.example.ui.theme.AppBackground
import com.example.ui.theme.CardBackground
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.OrchidPrimary
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun ReportUserScreen(
    user: User,
    onSubmit: suspend (reason: String, details: String) -> Boolean,
    onBack: () -> Unit
) {
    val reasons = listOf(
        "Inappropriate content",
        "Harassment or bullying",
        "Scam or fraud",
        "Fake profile",
        "Spam",
        "Other"
    )
    var selectedReason by remember { mutableStateOf<String?>(null) }
    var details by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("report_user_screen")
    ) {
        SoulTopBar(title = "Report ${user.name}", showBack = true, onBackClick = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(20.dp)
        ) {
            Text(
                text = "Why are you reporting this profile?",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            reasons.forEach { reason ->
                val isSelected = reason == selectedReason
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) OrchidPrimary else TextSecondary.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .background(CardBackground, RoundedCornerShape(14.dp))
                        .clickable { selectedReason = reason }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .testTag("report_reason_${reason.lowercase().replace(" ", "_")}")
                ) {
                    Text(
                        text = reason,
                        fontFamily = InterFontFamily,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            SoulTextField(
                label = "Additional details (optional)",
                value = details,
                onValueChange = { if (it.length <= 500) details = it },
                placeholder = "Add context that may help review this report.",
                singleLine = false,
                maxLines = 6
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${details.length}/500",
                fontFamily = InterFontFamily,
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            GradientButton(
                text = if (isSubmitting) "Submitting..." else "Submit Report",
                enabled = selectedReason != null && !isSubmitting,
                onClick = {
                    val reason = selectedReason ?: return@GradientButton
                    scope.launch {
                        isSubmitting = true
                        val submitted = onSubmit(reason, details.trim())
                        isSubmitting = false
                        if (submitted) {
                            Toast.makeText(context, "Report submitted.", Toast.LENGTH_LONG).show()
                            onBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
