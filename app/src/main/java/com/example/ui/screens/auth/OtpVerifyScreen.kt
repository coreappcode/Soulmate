package com.example.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GradientButton
import com.example.ui.components.OtpInput
import com.example.ui.theme.AppBackground
import com.example.ui.theme.AuroraHorizontalGradient
import com.example.ui.theme.CardBackground
import com.example.ui.theme.BodyStyle
import com.example.ui.theme.H2Style
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.OrchidPrimary
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.SemanticError
import com.example.ui.theme.SemanticSuccess
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OtpVerifyScreen(
    countryDialCode: String,
    phoneNumber: String,
    otp: String,
    onOtpChange: (String) -> Unit,
    onVerifySuccess: () -> Unit,
    onChangeNumber: () -> Unit,
    onBack: () -> Unit
) {
    var timer by remember { mutableIntStateOf(60) }
    var isVerifying by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }
    var checkScale by remember { mutableFloatStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()

    // Countdown timer
    LaunchedEffect(Unit) {
        while (timer > 0) {
            delay(1000)
            timer -= 1
        }
    }

    fun triggerVerify(code: String) {
        if (code.length == 6 && !isVerifying && !isSuccess) {
            isVerifying = true
            isError = false
            coroutineScope.launch {
                delay(1200)
                isVerifying = false
                isSuccess = true
                delay(800)
                onVerifySuccess()
            }
        }
    }

    LaunchedEffect(isSuccess) {
        if (isSuccess) checkScale = 1f
    }

    val successScale by animateFloatAsState(
        targetValue = checkScale,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "check_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
            .navigationBarsPadding()
            .testTag("otp_verify_screen")
    ) {
        // Back arrow
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(top = 8.dp)
                .size(40.dp)
                .testTag("otp_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Enter the code",
            style = H2Style
        )

        Spacer(modifier = Modifier.height(8.dp))

        val displayPhone = if (phoneNumber.isNotBlank()) {
            "$countryDialCode $phoneNumber"
        } else {
            "+91 98765 43210"
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sent to $displayPhone",
                style = BodyStyle.copy(color = TextSecondary)
            )
            Text(
                text = " · ",
                style = BodyStyle.copy(color = TextMuted)
            )
            Text(
                text = "Change",
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = OrchidPrimary,
                modifier = Modifier
                    .clickable { onChangeNumber() }
                    .testTag("change_phone_number")
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        OtpInput(
            length = 6,
            otp = otp,
            onOtpChange = { newOtp ->
                isError = false
                onOtpChange(newOtp)
            },
            onComplete = { completedOtp ->
                triggerVerify(completedOtp)
            },
            isError = isError,
            modifier = Modifier.fillMaxWidth()
        )

        if (isError) {
            Text(
                text = "Invalid code. Please try again.",
                fontFamily = InterFontFamily,
                fontSize = 13.sp,
                color = SemanticError,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 10.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Verify button
        GradientButton(
            text = "Verify",
            onClick = { triggerVerify(otp) },
            enabled = otp.length == 6 && !isVerifying && !isSuccess,
            loading = isVerifying,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Timer or Resend Code row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (timer > 0) {
                Text(
                    text = "Resend in 0:${if (timer < 10) "0$timer" else "$timer"}",
                    fontFamily = InterFontFamily,
                    fontSize = 14.sp,
                    color = TextMuted
                )
            } else {
                Text(
                    text = "Resend Code",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = OrchidPrimary,
                    modifier = Modifier
                        .clickable {
                            timer = 60
                            onOtpChange("")
                        }
                        .testTag("resend_code_button")
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Verification status indicator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isVerifying) {
                CircularProgressIndicator(
                    color = OrchidPrimary,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(32.dp)
                )
            } else if (isSuccess) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .scale(successScale)
                        .clip(CircleShape)
                        .background(AuroraHorizontalGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Verified",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}