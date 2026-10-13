package com.example.ui.screens.profile_setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.ui.components.ProgressDots
import com.example.ui.components.SoulTextField
import com.example.ui.theme.AppBackground
import com.example.ui.theme.H2Style
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.OrchidPrimary
import com.example.ui.theme.SemanticError
import com.example.ui.theme.SemanticSuccess
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.utils.Validators
import kotlinx.coroutines.delay

private enum class AvailabilityState {
    IDLE,
    CHECKING,
    AVAILABLE,
    TAKEN
}

@Composable
fun UsernameScreen(
    username: String,
    onUsernameChange: (String) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    var availability by remember { mutableStateOf(AvailabilityState.IDLE) }

    val hasLength = Validators.isValidUsernameLength(username)
    val hasValidChars = Validators.isValidUsernameChars(username)

    // Debounced fake availability check
    LaunchedEffect(username) {
        if (hasLength && hasValidChars) {
            availability = AvailabilityState.CHECKING
            delay(800)
            availability = if (username.lowercase() == "admin" || username.lowercase() == "soulmate") {
                AvailabilityState.TAKEN
            } else {
                AvailabilityState.AVAILABLE
            }
        } else {
            availability = AvailabilityState.IDLE
        }
    }

    val allGreen = hasLength && hasValidChars && availability == AvailabilityState.AVAILABLE

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("username_screen")
    ) {
        // Top row with back button and ProgressDots
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.CenterStart)
                    .testTag("username_back")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }

            ProgressDots(
                total = 5,
                current = 1,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Pick your username",
                style = H2Style
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "This is how others will find you.",
                fontFamily = InterFontFamily,
                fontSize = 14.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(32.dp))

            SoulTextField(
                label = "Username",
                value = username,
                onValueChange = { input ->
                    val lower = input.lowercase().filter { it.isLetterOrDigit() || it == '_' }
                    if (lower.length <= 45) {
                        onUsernameChange(lower)
                    }
                },
                placeholder = "username",
                leadingIcon = {
                    Text(
                        text = "@",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = OrchidPrimary,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Validation Rule 1: Min 5 chars
            ValidationRow(
                text = "At least 5 characters",
                status = when {
                    username.isEmpty() -> RowStatus.PENDING
                    hasLength -> RowStatus.PASSED
                    else -> RowStatus.FAILED
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Validation Rule 2: Only lowercase, numbers, underscore
            ValidationRow(
                text = "Only lowercase, numbers, underscore",
                status = when {
                    username.isEmpty() -> RowStatus.PENDING
                    hasValidChars -> RowStatus.PASSED
                    else -> RowStatus.FAILED
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Validation Rule 3: Availability check
            AvailabilityRow(state = availability)
        }

        // Bottom sticky button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            GradientButton(
                text = "Continue",
                onClick = onContinue,
                enabled = allGreen,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private enum class RowStatus {
    PENDING,
    PASSED,
    FAILED
}

@Composable
private fun ValidationRow(
    text: String,
    status: RowStatus
) {
    val (icon, color) = when (status) {
        RowStatus.PASSED -> Icons.Filled.CheckCircle to SemanticSuccess
        RowStatus.FAILED -> Icons.Filled.HighlightOff to SemanticError
        RowStatus.PENDING -> Icons.Outlined.Circle to TextMuted
    }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = text,
            fontFamily = InterFontFamily,
            fontSize = 13.sp,
            color = color
        )
    }
}

@Composable
private fun AvailabilityRow(
    state: AvailabilityState
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (state) {
            AvailabilityState.IDLE -> {
                Icon(
                    imageVector = Icons.Outlined.Circle,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Username availability",
                    fontFamily = InterFontFamily,
                    fontSize = 13.sp,
                    color = TextMuted
                )
            }
            AvailabilityState.CHECKING -> {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    color = OrchidPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Checking availability...",
                    fontFamily = InterFontFamily,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
            AvailabilityState.AVAILABLE -> {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = SemanticSuccess,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Username available",
                    fontFamily = InterFontFamily,
                    fontSize = 13.sp,
                    color = SemanticSuccess
                )
            }
            AvailabilityState.TAKEN -> {
                Icon(
                    imageVector = Icons.Filled.HighlightOff,
                    contentDescription = null,
                    tint = SemanticError,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Username already taken",
                    fontFamily = InterFontFamily,
                    fontSize = 13.sp,
                    color = SemanticError
                )
            }
        }
    }
}
