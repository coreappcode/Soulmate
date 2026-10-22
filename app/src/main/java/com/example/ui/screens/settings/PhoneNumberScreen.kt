package com.example.ui.screens.settings

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Country
import com.example.ui.components.GradientButton
import com.example.ui.components.OtpInput
import com.example.ui.components.PhoneInput
import com.example.ui.components.SoulTopBar
import com.example.ui.theme.AppBackground
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.OrchidPrimary
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.SemanticError
import com.example.ui.theme.SemanticSuccess
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.utils.Validators
import kotlinx.coroutines.delay

@Composable
fun PhoneNumberScreen(
    currentCountry: Country,
    currentPhone: String,
    otp: String,
    isSendingCode: Boolean,
    isVerifying: Boolean,
    isPhoneUpdate: Boolean,
    isPhoneUpdateComplete: Boolean,
    codeSentVersion: Int,
    errorMessage: String?,
    onOtpChange: (String) -> Unit,
    onRequestCode: (String) -> Unit,
    onVerifyCode: (String) -> Unit,
    onResendCode: (String) -> Unit,
    onChangeNumber: () -> Unit,
    onPhoneUpdated: () -> Unit,
    onOpenCountryPicker: () -> Unit,
    onBack: () -> Unit
) {
    var phone by remember(currentPhone) {
        mutableStateOf(
            currentPhone.removePrefix(currentCountry.dialCode).filter(Char::isDigit)
        )
    }
    val isValid = Validators.isValidPhoneNumber(phone)
    val isCodeSent = isPhoneUpdate && codeSentVersion > 0
    var timer by remember { mutableIntStateOf(0) }

    LaunchedEffect(codeSentVersion) {
        if (codeSentVersion > 0) {
            timer = 60
            while (timer > 0) {
                delay(1000)
                timer -= 1
            }
        } else {
            timer = 0
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("phone_number_screen")
    ) {
        SoulTopBar(
            title = "Phone Number",
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
                text = "Update Phone Number",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isCodeSent) {
                    "Enter the 6-digit code sent to your new number."
                } else {
                    "We will send an SMS verification code to verify your new number."
                },
                fontFamily = InterFontFamily,
                fontSize = 13.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isCodeSent || isPhoneUpdateComplete) {
                Text(
                    text = "${currentCountry.dialCode} $phone",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Text(
                    text = "Change number",
                    fontFamily = InterFontFamily,
                    fontSize = 14.sp,
                    color = OrchidPrimary,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clickable(onClick = onChangeNumber)
                )
            } else {
                PhoneInput(
                    countryCode = currentCountry.dialCode,
                    countryFlag = currentCountry.flag,
                    phoneNumber = phone,
                    onCountryTap = onOpenCountryPicker,
                    onNumberChange = { phone = it },
                    isError = phone.isNotEmpty() && !isValid,
                    supportingText = if (phone.isNotEmpty() && !isValid) "Please enter a valid phone number" else null
                )
            }

            if (isCodeSent && !isPhoneUpdateComplete) {
                Spacer(modifier = Modifier.height(28.dp))
                OtpInput(
                    length = 6,
                    otp = otp,
                    onOtpChange = onOtpChange,
                    onComplete = onVerifyCode,
                    isError = errorMessage != null,
                    modifier = Modifier.fillMaxWidth()
                )
                if (timer > 0) {
                    Text(
                        text = "Resend in 0:${if (timer < 10) "0$timer" else "$timer"}",
                        fontFamily = InterFontFamily,
                        fontSize = 14.sp,
                        color = TextMuted,
                        modifier = Modifier
                            .align(androidx.compose.ui.Alignment.CenterHorizontally)
                            .padding(top = 20.dp)
                    )
                } else if (!isSendingCode && !isVerifying) {
                    Text(
                        text = "Resend code",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = OrchidPrimary,
                        modifier = Modifier
                            .align(androidx.compose.ui.Alignment.CenterHorizontally)
                            .padding(top = 20.dp)
                            .clickable { onResendCode(phone) }
                    )
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    fontFamily = InterFontFamily,
                    fontSize = 13.sp,
                    color = SemanticError,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            if (isPhoneUpdateComplete) {
                Text(
                    text = "Phone number verified and updated.",
                    fontFamily = InterFontFamily,
                    fontSize = 14.sp,
                    color = SemanticSuccess,
                    modifier = Modifier.padding(top = 20.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppBackground)
                .border(width = 1.dp, color = Color(0xFF000000).copy(alpha = 0.08f))
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            GradientButton(
                text = when {
                    isPhoneUpdateComplete -> "Done"
                    isCodeSent -> "Verify Phone Number"
                    else -> "Send Verification Code"
                },
                onClick = {
                    when {
                        isPhoneUpdateComplete -> onPhoneUpdated()
                        isCodeSent -> onVerifyCode(otp)
                        else -> onRequestCode(phone)
                    }
                },
                enabled = when {
                    isPhoneUpdateComplete -> true
                    isCodeSent -> otp.length == 6 && !isVerifying && !isSendingCode
                    else -> isValid && !isSendingCode
                },
                loading = isSendingCode || isVerifying,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
