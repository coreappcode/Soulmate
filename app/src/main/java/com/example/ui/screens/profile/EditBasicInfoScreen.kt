package com.example.ui.screens.profile

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GradientButton
import com.example.ui.components.SoulTextField
import com.example.ui.components.SoulTopBar
import com.example.ui.theme.AppBackground
import com.example.ui.theme.AuroraHorizontalGradient
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.OrchidPrimary
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.SemanticOnline
import com.example.ui.theme.SemanticSuccess
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.utils.Validators

@Composable
fun EditBasicInfoScreen(
    currentName: String,
    currentDob: String,
    currentGender: String,
    currentCity: String,
    onSave: (name: String, dob: String, gender: String, city: String) -> Unit,
    onOpenDatePicker: () -> Unit,
    onBack: () -> Unit,
    isSaving: Boolean = false
) {
    var name by remember { mutableStateOf(currentName) }
    var dob by remember(currentDob) { mutableStateOf(currentDob) }
    var gender by remember { mutableStateOf(currentGender) }
    var city by remember { mutableStateOf(currentCity) }

    val isNameValid = Validators.isValidName(name)
    val isDateOfBirthValid = Validators.isValidAdultDateOfBirth(dob)
    val canSave = isNameValid && isDateOfBirthValid && gender.isNotBlank()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("edit_basic_info_screen")
    ) {
        SoulTopBar(
            title = "Basic Info",
            showBack = true,
            onBackClick = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(20.dp)
        ) {
            // Full Name input with real-time validation & trailing icon
            SoulTextField(
                label = "Full Name",
                value = name,
                onValueChange = { name = it },
                placeholder = "Enter your full name",
                trailingIcon = {
                    if (name.isNotEmpty()) {
                        Icon(
                            imageVector = if (isNameValid) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (isNameValid) SemanticSuccess else Color.Red,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                supportingText = {
                    if (name.isNotEmpty() && !isNameValid) {
                        Text(
                            text = "Name must be between 2 and 50 characters",
                            fontSize = 11.sp,
                            color = Color.Red
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Date of Birth input with calendar tap -> Full Screen DatePicker
            SoulTextField(
                label = "Date of Birth",
                value = dob,
                onValueChange = { dob = it },
                placeholder = "DD/MM/YYYY",
                readOnly = true,
                onClick = onOpenDatePicker,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Pick Date",
                        tint = OrchidPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                supportingText = {
                    if (dob.isNotBlank() && !isDateOfBirthValid) {
                        Text(
                            text = "Choose a valid date of birth. You must be 18 or older.",
                            fontSize = 11.sp,
                            color = Color.Red
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // City input
            SoulTextField(
                label = "City",
                value = city,
                onValueChange = { if (it.length <= 100) city = it },
                placeholder = "e.g. Mumbai, New York"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Gender Selection (3 gradient-bordered cards: Woman, Man, Non-binary)
            Text(
                text = "Gender",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            val genderOptions = listOf("Woman", "Man", "Non-binary")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                genderOptions.forEach { option ->
                    val isSelected = gender == option
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(
                                width = if (isSelected) 2.5.dp else 1.5.dp,
                                color = if (isSelected) OrchidPrimary else Color(0xFF000000),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { gender = option }
                            .padding(horizontal = 18.dp, vertical = 14.dp)
                            .testTag("gender_option_${option.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = option,
                                fontFamily = PoppinsFontFamily,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(AuroraHorizontalGradient),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Sticky save bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppBackground)
                .border(width = 1.dp, color = Color(0xFF000000).copy(alpha = 0.08f))
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            GradientButton(
                text = "Save Changes",
                onClick = { onSave(name, dob, gender, city) },
                enabled = canSave && !isSaving,
                loading = isSaving,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
