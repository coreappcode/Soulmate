package com.example.ui.screens.auth

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GradientButton
import com.example.ui.components.OtpInput
import com.example.ui.theme.AppBackground
import com.example.ui.theme.BodyStyle
import com.example.ui.theme.H2Style
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.OrchidPrimary
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.SemanticError
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun OtpVerifyScreen(
    countryDialCode: String,
    phoneNumber: String,
    otp: String,
    isVerifying: Boolean,
    isSendingCode: Boolean,
    errorMessage: String?,
    codeSentVersion: Int,
    onOtpChange: (String) -> Unit,
    onVerify: (String) -> Unit,
    onResendCode: () -> Unit,
    onChangeNumber: () -> Unit,
    onBack: () -> Unit
) {
    var timer by remember { mutableIntStateOf(60) }

    LaunchedEffect(codeSentVersion) {
        timer = 60
        while (timer > 0) {
            delay(1000)
            timer -= 1
        }
    }

    fun triggerVerify(code: String) {
        if (code.length == 6 && !isVerifying && !isSendingCode) {
            onVerify(code)
        }
    }

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

        val displayPhone = "$countryDialCode $phoneNumber"

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
                onOtpChange(newOtp)
            },
            onComplete = { completedOtp ->
                triggerVerify(completedOtp)
            },
            isError = errorMessage != null,
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage,
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
            enabled = otp.length == 6 && !isVerifying && !isSendingCode,
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
            if (isSendingCode) {
                Text(
                    text = "Sending code...",
                    fontFamily = InterFontFamily,
                    fontSize = 14.sp,
                    color = TextMuted
                )
            } else if (timer > 0) {
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
                            onOtpChange("")
                            onResendCode()
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
            if (isVerifying || isSendingCode) {
                CircularProgressIndicator(
                    color = OrchidPrimary,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}