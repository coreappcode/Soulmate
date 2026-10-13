package com.example.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GradientButton
import com.example.ui.components.SoulTextField
import com.example.ui.components.SoulTopBar
import com.example.ui.theme.AppBackground
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.OrchidPrimary
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.SemanticOnline
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.utils.Validators

@Composable
fun EmailScreen(
    currentEmail: String,
    onSave: (String) -> Unit,
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf(currentEmail) }
    val isValid = Validators.isValidEmail(email)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("email_screen")
    ) {
        SoulTopBar(
            title = "Email Address",
            showBack = true,
            onBackClick = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Update Email Address",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "We will send receipts, account security alerts, and weekly match roundups to this address.",
                fontFamily = InterFontFamily,
                fontSize = 13.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            SoulTextField(
                label = "Email Address",
                value = email,
                onValueChange = { email = it.trim() },
                placeholder = "your.name@example.com",
                keyboardType = KeyboardType.Email,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = OrchidPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (email.isNotEmpty()) {
                        Icon(
                            imageVector = if (isValid) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (isValid) SemanticOnline else Color.Red,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                supportingText = {
                    if (email.isNotEmpty() && !isValid) {
                        Text(
                            text = "Please enter a valid email address",
                            fontSize = 11.sp,
                            color = Color.Red
                        )
                    }
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppBackground)
                .border(width = 1.dp, color = Color(0xFF000000).copy(alpha = 0.08f))
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            GradientButton(
                text = "Save Email",
                onClick = { onSave(email) },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
