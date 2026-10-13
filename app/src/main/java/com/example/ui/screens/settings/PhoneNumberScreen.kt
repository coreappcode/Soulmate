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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Country
import com.example.ui.components.GradientButton
import com.example.ui.components.PhoneInput
import com.example.ui.components.SoulTopBar
import com.example.ui.theme.AppBackground
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.utils.Validators

@Composable
fun PhoneNumberScreen(
    currentCountry: Country,
    currentPhone: String,
    onSave: (String) -> Unit,
    onOpenCountryPicker: () -> Unit,
    onBack: () -> Unit
) {
    var phone by remember { mutableStateOf(currentPhone) }
    val isValid = Validators.isValidPhoneNumber(phone)

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
                text = "We will send an SMS verification code to verify your new number.",
                fontFamily = InterFontFamily,
                fontSize = 13.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppBackground)
                .border(width = 1.dp, color = Color(0xFF000000).copy(alpha = 0.08f))
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            GradientButton(
                text = "Save Phone Number",
                onClick = { onSave(phone) },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
