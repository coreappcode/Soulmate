package com.example.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.ui.components.GradientButton
import com.example.ui.components.SoulTextField
import com.example.ui.components.SoulTopBar
import com.example.ui.theme.AppBackground
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.TextSecondary

@Composable
fun EditProfileDetailsScreen(
    currentHeight: String,
    currentJob: String,
    currentEducation: String,
    currentZodiac: String,
    onSave: (height: String, job: String, education: String, zodiac: String) -> Unit,
    onBack: () -> Unit,
    isSaving: Boolean = false
) {
    var height by remember(currentHeight) { mutableStateOf(currentHeight) }
    var job by remember(currentJob) { mutableStateOf(currentJob) }
    var education by remember(currentEducation) { mutableStateOf(currentEducation) }
    var zodiac by remember(currentZodiac) { mutableStateOf(currentZodiac) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("edit_profile_details_screen")
    ) {
        SoulTopBar(
            title = "Profile Details",
            showBack = true,
            onBackClick = onBack
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "Add optional details to your profile.",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(18.dp))
            DetailsField("Height", height) { height = it }
            Spacer(modifier = Modifier.height(16.dp))
            DetailsField("Work", job) { job = it }
            Spacer(modifier = Modifier.height(16.dp))
            DetailsField("Education", education) { education = it }
            Spacer(modifier = Modifier.height(16.dp))
            DetailsField("Zodiac", zodiac) { zodiac = it }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppBackground)
                .border(1.dp, Color(0xFF000000).copy(alpha = 0.08f))
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            GradientButton(
                text = "Save Details",
                onClick = { onSave(height.trim(), job.trim(), education.trim(), zodiac.trim()) },
                enabled = !isSaving,
                loading = isSaving,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun DetailsField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    SoulTextField(
        label = label,
        value = value,
        onValueChange = { if (it.length <= 80) onValueChange(it) },
        placeholder = "Optional",
        supportingText = {
            Text(
                text = "${value.length}/80",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    )
}
