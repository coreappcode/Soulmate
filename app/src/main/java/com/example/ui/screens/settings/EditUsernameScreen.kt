package com.example.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
import kotlinx.coroutines.delay

@Composable
fun EditUsernameScreen(
    currentUsername: String,
    onSave: (String) -> Unit,
    onBack: () -> Unit
) {
    var username by remember { mutableStateOf(currentUsername) }
    var isChecking by remember { mutableStateOf(false) }
    var isAvailable by remember { mutableStateOf(true) }

    val isValidFormat = Validators.isValidUsername(username)

    LaunchedEffect(username) {
        if (isValidFormat && username.length >= 5) {
            isChecking = true
            delay(400)
            isChecking = false
            isAvailable = !listOf("admin", "soulmate", "help", "support").contains(username.lowercase())
        } else {
            isChecking = false
            isAvailable = false
        }
    }

    val canSave = isValidFormat && isAvailable && !isChecking

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("edit_username_screen")
    ) {
        SoulTopBar(
            title = "Change Username",
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
                text = "Pick a Unique Username",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Usernames must be 5-45 characters and can contain lowercase letters, numbers, and underscores.",
                fontFamily = InterFontFamily,
                fontSize = 13.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            SoulTextField(
                label = "Username",
                value = username,
                onValueChange = { input ->
                    val filtered = input.lowercase().filter { it.isLetterOrDigit() || it == '_' }
                    if (filtered.length <= 45) username = filtered
                },
                placeholder = "username",
                leadingIcon = {
                    Text(
                        text = "@",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = OrchidPrimary,
                        modifier = Modifier.padding(start = 14.dp, end = 4.dp)
                    )
                },
                trailingIcon = {
                    if (isChecking) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = OrchidPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    } else if (username.isNotEmpty()) {
                        Icon(
                            imageVector = if (canSave) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (canSave) SemanticOnline else Color.Red,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                supportingText = {
                    if (username.isNotEmpty() && !isValidFormat) {
                        Text(
                            text = "5-45 characters, letters, numbers, and _ only",
                            fontSize = 11.sp,
                            color = Color.Red
                        )
                    } else if (canSave) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = SemanticOnline,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "@$username is available!",
                                fontSize = 11.sp,
                                color = SemanticOnline
                            )
                        }
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
                text = "Save Username",
                onClick = { onSave(username) },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
