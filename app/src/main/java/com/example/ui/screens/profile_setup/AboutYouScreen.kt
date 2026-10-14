package com.example.ui.screens.profile_setup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GradientButton
import com.example.ui.components.ProgressDots
import com.example.ui.components.SoulChip
import com.example.ui.theme.AppBackground
import com.example.ui.theme.AuroraHorizontalGradient
import com.example.ui.theme.H2Style
import com.example.ui.theme.InputShape
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.SemanticError
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.utils.Constants

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AboutYouScreen(
    bio: String,
    selectedInterests: List<String>,
    selectedLookingFor: List<String>,
    onBioChange: (String) -> Unit,
    onToggleInterest: (String) -> Unit,
    onToggleLookingFor: (String) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    // Validations: Bio minimum 10 chars, at least 1 interest, exactly 1 option chosen for looking for
    val isBioValid = bio.trim().length in 10..500
    val isInterestsValid = selectedInterests.isNotEmpty() && selectedInterests.size <= 10
    val isLookingForValid = selectedLookingFor.isNotEmpty()

    val canContinue = isBioValid && isInterestsValid && isLookingForValid

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("about_you_screen")
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
                    .testTag("about_you_back")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }

            ProgressDots(
                total = 4,
                current = 3,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Almost there!",
                style = H2Style
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Tell potential soulmates what makes you unique.",
                fontFamily = InterFontFamily,
                fontSize = 14.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Section 1: Bio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bio *",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )

                Text(
                    text = "${bio.length}/500",
                    fontFamily = InterFontFamily,
                    fontSize = 12.sp,
                    color = if (bio.length > 500) SemanticError else TextMuted
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = bio,
                onValueChange = { if (it.length <= 600) onBioChange(it) },
                placeholder = {
                    Text(
                        text = "A quick snippet about your passions, quirks, and dream Sunday afternoon... (min 10 characters)",
                        fontFamily = InterFontFamily,
                        fontSize = 14.sp,
                        color = TextMuted
                    )
                },
                maxLines = 5,
                minLines = 4,
                shape = InputShape,
                textStyle = TextStyle(
                    fontFamily = InterFontFamily,
                    fontSize = 15.sp,
                    color = TextPrimary
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Color(0xFF000000), InputShape)
                    .testTag("bio_input")
            )

            if (bio.isNotEmpty() && bio.trim().length < 10) {
                Text(
                    text = "${10 - bio.trim().length} more characters required for bio",
                    fontFamily = InterFontFamily,
                    fontSize = 12.sp,
                    color = SemanticError,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Section 2: Interests
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Interests *",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
                Text(
                    text = "${selectedInterests.size}/10 selected",
                    fontFamily = InterFontFamily,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Constants.PRESET_INTERESTS.forEach { interest ->
                    val isSelected = selectedInterests.contains(interest)
                    SoulChip(
                        text = interest,
                        selected = isSelected,
                        onClick = { onToggleInterest(interest) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Section 3: Who are you looking for? (Single selection)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Who are you looking for? *",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2x2 grid for Looking For options — Strictly single selection
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LookingForCard(
                        title = "Men",
                        isSelected = selectedLookingFor.contains("Men"),
                        onClick = { onToggleLookingFor("Men") },
                        modifier = Modifier.weight(1f)
                    )
                    LookingForCard(
                        title = "Women",
                        isSelected = selectedLookingFor.contains("Women"),
                        onClick = { onToggleLookingFor("Women") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LookingForCard(
                        title = "Non-binary",
                        isSelected = selectedLookingFor.contains("Non-binary"),
                        onClick = { onToggleLookingFor("Non-binary") },
                        modifier = Modifier.weight(1f)
                    )
                    LookingForCard(
                        title = "All",
                        isSelected = selectedLookingFor.contains("All"),
                        onClick = { onToggleLookingFor("All") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        // Bottom sticky button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            if (!canContinue) {
                val missing = mutableListOf<String>().apply {
                    if (!isBioValid) add("Bio (min 10 chars)")
                    if (!isInterestsValid) add("Select at least 1 Interest")
                    if (!isLookingForValid) add("Select who you are looking for")
                }.joinToString(" • ")

                Text(
                    text = missing,
                    fontFamily = InterFontFamily,
                    fontSize = 12.sp,
                    color = SemanticError,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }

            GradientButton(
                text = "Continue",
                onClick = onContinue,
                enabled = canContinue,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun LookingForCard(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(18.dp)

    Box(
        modifier = modifier
            .height(58.dp)
            .clip(cardShape)
            .then(
                if (isSelected) {
                    Modifier
                        .background(AuroraHorizontalGradient)
                        .border(2.5.dp, Color(0xFF000000), cardShape)
                } else {
                    Modifier
                        .background(Color.White)
                        .border(2.dp, Color(0xFF000000), cardShape)
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontFamily = PoppinsFontFamily,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 15.sp,
                color = if (isSelected) Color.White else TextPrimary
            )
            if (isSelected) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
