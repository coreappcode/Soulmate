package com.example.ui.screens.auth

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.Country
import com.example.ui.components.GradientButton
import com.example.ui.components.PhoneInput
import com.example.ui.components.ProgressDots
import com.example.ui.theme.AppBackground
import com.example.ui.theme.BodyStyle
import com.example.ui.theme.H2Style
import com.example.ui.theme.SemanticError
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.utils.Validators

@Composable
fun PhoneEntryScreen(
    country: Country,
    phoneNumber: String,
    isLoading: Boolean,
    errorMessage: String?,
    onPhoneNumberChange: (String) -> Unit,
    onCountryTap: () -> Unit,
    onSendCode: () -> Unit,
    onBack: () -> Unit
) {
    val isValid = Validators.isValidPhoneNumber(phoneNumber)
    val showError = !isValid && phoneNumber.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
            .navigationBarsPadding()
            .testTag("phone_entry_screen")
    ) {
        // Top row with back button and centered ProgressDots
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.CenterStart)
                    .testTag("phone_entry_back")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }

            ProgressDots(
                total = 3,
                current = 0,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "What's your number?",
            style = H2Style
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "We'll send you a 6-digit code to verify.",
            style = BodyStyle.copy(color = TextSecondary)
        )

        Spacer(modifier = Modifier.height(32.dp))

        PhoneInput(
            countryCode = country.dialCode,
            countryFlag = country.flag,
            phoneNumber = phoneNumber,
            onCountryTap = onCountryTap,
            onNumberChange = onPhoneNumberChange,
            isError = showError,
            supportingText = if (showError) "Please enter at least 10 digits" else null
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = BodyStyle.copy(color = SemanticError),
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        GradientButton(
            text = "Send Code",
            onClick = onSendCode,
            enabled = isValid && !isLoading,
            loading = isLoading,
            modifier = Modifier.fillMaxWidth()
        )
    }
}