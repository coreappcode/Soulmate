package com.example.ui.screens.settings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.ui.MatchmakingState
import com.example.ui.components.GradientButton
import com.example.ui.components.SoulTopBar
import com.example.ui.theme.AppBackground
import com.example.ui.theme.AuroraHorizontalGradient
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.OrchidPrimary
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun MatchmakingScreen(
    currentSettings: MatchmakingState,
    onSave: (MatchmakingState) -> Unit,
    onBack: () -> Unit
) {
    var showMe by remember { mutableStateOf(currentSettings.showMe) }
    var ageRange by remember { mutableStateOf(currentSettings.ageRange) }
    var distance by remember { mutableFloatStateOf(currentSettings.distance) }
    var countryToggle by remember { mutableStateOf(currentSettings.countryToggle) }
    var globalToggle by remember { mutableStateOf(currentSettings.globalToggle) }

    val showMeOptions = listOf("Women", "Men", "Non-binary", "Everyone")
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("matchmaking_screen")
    ) {
        SoulTopBar(
            title = "Matchmaking",
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
            // Show Me (4 radio cards)
            Text(
                text = "Show Me",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                showMeOptions.forEach { option ->
                    val isSelected = showMe == option
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isSelected) 2.5.dp else 1.5.dp,
                                color = if (isSelected) OrchidPrimary else Color(0xFF000000),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { showMe = option }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = option,
                                fontFamily = PoppinsFontFamily,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                fontSize = 14.sp,
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

            Spacer(modifier = Modifier.height(24.dp))

            // Age RangeSlider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Age Range",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Text(
                    text = "${ageRange.start.roundToInt()} - ${ageRange.endInclusive.roundToInt()}",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = OrchidPrimary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            RangeSlider(
                value = ageRange,
                onValueChange = { ageRange = it },
                valueRange = 18f..65f,
                steps = 46,
                colors = SliderDefaults.colors(
                    thumbColor = OrchidPrimary,
                    activeTrackColor = OrchidPrimary,
                    inactiveTrackColor = ElevatedSurface
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Maximum Distance Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Maximum Distance",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Text(
                    text = "${distance.roundToInt()} km",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = OrchidPrimary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Slider(
                value = distance,
                onValueChange = { distance = it },
                valueRange = 5f..150f,
                steps = 28,
                colors = SliderDefaults.colors(
                    thumbColor = OrchidPrimary,
                    activeTrackColor = OrchidPrimary,
                    inactiveTrackColor = ElevatedSurface
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Mutually exclusive Country & Global toggles
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color(0xFF000000), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Entire Country Only",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Expand search across your whole nation",
                                fontFamily = InterFontFamily,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        Switch(
                            checked = countryToggle,
                            onCheckedChange = { checked ->
                                countryToggle = checked
                                if (checked) globalToggle = false
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = OrchidPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Global Passport",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Explore matches worldwide without distance limits",
                                fontFamily = InterFontFamily,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        Switch(
                            checked = globalToggle,
                            onCheckedChange = { checked ->
                                globalToggle = checked
                                if (checked) countryToggle = false
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = OrchidPrimary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
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
                text = "Save Matchmaking Settings",
                onClick = {
                    onSave(
                        MatchmakingState(
                            showMe = showMe,
                            ageRange = ageRange,
                            distance = distance,
                            countryToggle = countryToggle,
                            globalToggle = globalToggle
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
