package com.example.ui.screens.profile_setup

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.ContextWrapper
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Transgender
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog as ComposeDatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GradientButton
import com.example.ui.components.ProgressDots
import com.example.ui.components.SoulTextField
import com.example.ui.theme.AppBackground
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.H2Style
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.OrchidPrimary
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Calendar
import java.util.TimeZone

private fun Context.findActivity(): Activity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicInfoScreen(
    fullName: String,
    dateOfBirth: String,
    gender: String,
    onFullNameChange: (String) -> Unit,
    onDateOfBirthChange: (String) -> Unit = {},
    onOpenDatePicker: () -> Unit = {},
    onGenderSelect: (String) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showComposeDatePicker by remember { mutableStateOf(false) }

    fun openCalendar() {
        val activity = context.findActivity()
        if (activity != null) {
            try {
                val dialog = DatePickerDialog(
                    activity,
                    { _, year, month, dayOfMonth ->
                        val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                        val formatted = "${monthNames[month]} $dayOfMonth, $year"
                        onDateOfBirthChange(formatted)
                    },
                    2000,
                    0,
                    1
                )
                dialog.datePicker.maxDate = System.currentTimeMillis()
                dialog.show()
            } catch (_: Exception) {
                showComposeDatePicker = true
            }
        } else {
            showComposeDatePicker = true
        }
    }

    val isFormValid = fullName.trim().length >= 2 && dateOfBirth.isNotBlank() && gender.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("basic_info_screen")
    ) {
        // Top header with back button and ProgressDots
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
                    .testTag("basic_info_back")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }

            ProgressDots(
                total = 5,
                current = 0,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Tell us about you",
                style = H2Style
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "This will be displayed on your public profile.",
                fontFamily = InterFontFamily,
                fontSize = 14.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Full Name input with solid black border
            SoulTextField(
                label = "Full Name",
                value = fullName,
                onValueChange = onFullNameChange,
                placeholder = "e.g. Maya Lin",
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Date of birth input with solid black border — tapping opens native Android calendar!
            SoulTextField(
                label = "Date of Birth",
                value = dateOfBirth,
                onValueChange = onDateOfBirthChange,
                placeholder = "Tap to choose date",
                readOnly = true,
                onClick = { openCalendar() },
                trailingIcon = {
                    IconButton(onClick = { openCalendar() }) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Pick Date",
                            tint = Color(0xFF000000)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Gender selection section with high-visibility black borders
            Text(
                text = "Gender",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GenderCard(
                    title = "Man",
                    icon = Icons.Default.Male,
                    isSelected = gender == "Man",
                    onClick = { onGenderSelect("Man") },
                    modifier = Modifier.weight(1f)
                )

                GenderCard(
                    title = "Woman",
                    icon = Icons.Default.Female,
                    isSelected = gender == "Woman",
                    onClick = { onGenderSelect("Woman") },
                    modifier = Modifier.weight(1f)
                )

                GenderCard(
                    title = "Non-binary",
                    icon = Icons.Default.Transgender,
                    isSelected = gender == "Non-binary",
                    onClick = { onGenderSelect("Non-binary") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Sticky bottom button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            GradientButton(
                text = "Continue",
                onClick = onContinue,
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (showComposeDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = 946684800000L // Jan 1, 2000
            )
            ComposeDatePickerDialog(
                onDismissRequest = { showComposeDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                    timeInMillis = millis
                                }
                                val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                                val formatted = "${monthNames[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.DAY_OF_MONTH)}, ${cal.get(Calendar.YEAR)}"
                                onDateOfBirthChange(formatted)
                            }
                            showComposeDatePicker = false
                        }
                    ) {
                        Text("OK", color = Color(0xFF000000), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showComposeDatePicker = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            ) {
                DatePicker(
                    state = datePickerState,
                    title = {
                        Text(
                            text = "Select Date of Birth",
                            modifier = Modifier.padding(start = 24.dp, top = 16.dp),
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = Color(0xFF000000)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun GenderCard(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)

    Column(
        modifier = modifier
            .height(100.dp)
            .clip(shape)
            .background(if (isSelected) ElevatedSurface else Color.White)
            .border(
                width = if (isSelected) 2.5.dp else 1.5.dp,
                color = Color(0xFF000000),
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
            .testTag("gender_card_${title.lowercase()}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isSelected) OrchidPrimary else Color(0xFF000000),
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            fontFamily = PoppinsFontFamily,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 13.sp,
            color = if (isSelected) OrchidPrimary else Color(0xFF000000)
        )
    }
}
