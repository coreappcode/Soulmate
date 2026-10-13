package com.example.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.NotificationSettingsState
import com.example.ui.components.GradientButton
import com.example.ui.components.SoulTopBar
import com.example.ui.theme.AppBackground
import com.example.ui.theme.CardBackground
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.OrchidPrimary
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun NotificationSettingsScreen(
    currentSettings: NotificationSettingsState,
    onSave: (NotificationSettingsState) -> Unit,
    onBack: () -> Unit
) {
    var pushMatches by remember { mutableStateOf(currentSettings.pushMatches) }
    var pushMessages by remember { mutableStateOf(currentSettings.pushMessages) }
    var emailSummaries by remember { mutableStateOf(currentSettings.emailSummaries) }
    var soundAndVibration by remember { mutableStateOf(currentSettings.soundAndVibration) }
    var marketingPromotions by remember { mutableStateOf(currentSettings.marketingPromotions) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("notification_settings_screen")
    ) {
        SoulTopBar(
            title = "Notifications",
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
            Text(
                text = "Activity Toggles",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color(0xFF000000), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    NotificationToggleItem("New Matches & Likes", "Notify when someone likes you or is a match", pushMatches) { pushMatches = it }
                    HorizontalDivider(color = Color(0xFF000000).copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 12.dp))
                    NotificationToggleItem("Chat Messages", "Instant alerts when matches send you a message", pushMessages) { pushMessages = it }
                    HorizontalDivider(color = Color(0xFF000000).copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 12.dp))
                    NotificationToggleItem("Weekly Email Summary", "Highlights of who viewed your profile", emailSummaries) { emailSummaries = it }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Sound & Preferences",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color(0xFF000000), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    NotificationToggleItem("Sound & Vibration", "Play audio cues for matches and incoming messages", soundAndVibration) { soundAndVibration = it }
                    HorizontalDivider(color = Color(0xFF000000).copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 12.dp))
                    NotificationToggleItem("Marketing & Offers", "Special promos, discounts, and event announcements", marketingPromotions) { marketingPromotions = it }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppBackground)
                .border(width = 1.dp, color = Color(0xFF000000).copy(alpha = 0.08f))
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            GradientButton(
                text = "Save Notification Preferences",
                onClick = {
                    onSave(
                        NotificationSettingsState(
                            pushMatches = pushMatches,
                            pushMessages = pushMessages,
                            emailSummaries = emailSummaries,
                            soundAndVibration = soundAndVibration,
                            marketingPromotions = marketingPromotions
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun NotificationToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
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

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = OrchidPrimary
            )
        )
    }
}
