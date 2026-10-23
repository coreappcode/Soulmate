package com.example.ui.screens.profile

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import com.example.ui.components.SoulChip
import com.example.ui.components.SoulTextField
import com.example.ui.components.SoulTopBar
import com.example.ui.theme.AppBackground
import com.example.ui.theme.AuroraHorizontalGradient
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.OrchidPrimary
import com.example.ui.theme.PillShape
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.utils.Constants

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditAboutScreen(
    currentBio: String,
    currentInterests: List<String>,
    currentLookingFor: List<String>,
    onSave: (bio: String, interests: List<String>, lookingFor: List<String>) -> Unit,
    onBack: () -> Unit
) {
    var bio by remember { mutableStateOf(currentBio) }
    var selectedInterests by remember { mutableStateOf(currentInterests) }
    var selectedLookingFor by remember { mutableStateOf(currentLookingFor) }

    val allInterests = (Constants.PRESET_INTERESTS + currentInterests).distinct()
    val lookingForDescriptions = mapOf(
        "Men" to "Looking to meet men",
        "Women" to "Looking to meet women",
        "Non-binary" to "Looking to meet non-binary people",
        "All" to "Open to people of all genders"
    )
    val lookingForOptions = Constants.LOOKING_FOR_OPTIONS.map { option ->
        option to lookingForDescriptions.getValue(option)
    }

    val scrollState = rememberScrollState()
    val canSave = selectedInterests.size in 1..10 && selectedLookingFor.size == 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("edit_about_screen")
    ) {
        SoulTopBar(
            title = "About & Preferences",
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
            // Bio section with 500 character counter
            Text(
                text = "About You",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Share your story, your quirky habits, or what makes an ideal Sunday morning.",
                fontFamily = InterFontFamily,
                fontSize = 13.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            SoulTextField(
                label = "",
                value = bio,
                onValueChange = { if (it.length <= 500) bio = it },
                placeholder = "Write a little bit about yourself...",
                singleLine = false,
                maxLines = 6,
                supportingText = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "${bio.length}/500",
                            fontFamily = InterFontFamily,
                            fontSize = 12.sp,
                            color = if (bio.length >= 480) OrchidPrimary else TextMuted
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Interests chip selector (3-10 required)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Interests",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Text(
                    text = "${selectedInterests.size}/10 selected (min 1)",
                    fontFamily = InterFontFamily,
                    fontSize = 12.sp,
                    color = if (selectedInterests.isEmpty()) Color.Red else OrchidPrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allInterests.forEach { interest ->
                    val isSelected = selectedInterests.contains(interest)
                    SoulChip(
                        text = interest,
                        selected = isSelected,
                        onClick = {
                            selectedInterests = if (isSelected) {
                                selectedInterests - interest
                            } else {
                                if (selectedInterests.size < 10) selectedInterests + interest else selectedInterests
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Looking For 4-card multi-select
            Text(
                text = "Who Are You Looking For?",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Choose the genders you'd like to meet.",
                fontFamily = InterFontFamily,
                fontSize = 13.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                lookingForOptions.forEach { (title, subtitle) ->
                    val isSelected = selectedLookingFor.contains(title)
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
                            .clickable {
                                selectedLookingFor = listOf(title)
                            }
                            .padding(16.dp)
                            .testTag("looking_for_${title.lowercase().replace(" ", "_")}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title,
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = subtitle,
                                    fontFamily = InterFontFamily,
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }

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

            Spacer(modifier = Modifier.height(30.dp))
        }

        // Sticky Save bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppBackground)
                .border(width = 1.dp, color = Color(0xFF000000).copy(alpha = 0.08f))
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            GradientButton(
                text = "Save Preferences",
                onClick = { onSave(bio, selectedInterests, selectedLookingFor) },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
