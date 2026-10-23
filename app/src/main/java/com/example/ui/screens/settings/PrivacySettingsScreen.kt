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
import com.example.ui.PrivacySettingsState
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
fun PrivacySettingsScreen(
    currentSettings: PrivacySettingsState,
    onSave: (PrivacySettingsState) -> Unit,
    onBack: () -> Unit
) {
    var showOnline by remember { mutableStateOf(currentSettings.showOnlineStatus) }
    var showDistance by remember { mutableStateOf(currentSettings.showDistance) }
    var showAge by remember { mutableStateOf(currentSettings.showAge) }
    var allowNonFriends by remember { mutableStateOf(currentSettings.allowNonFriendsMessaging) }
    var sendReadReceipts by remember { mutableStateOf(currentSettings.sendReadReceipts) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("privacy_settings_screen")
    ) {
        SoulTopBar(
            title = "Privacy & Visibility",
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
                text = "Online status is shared while you are active if this setting is enabled. Age visibility is enforced on public profiles; distance display, non-friend messaging, and read receipts are not configured.",
                fontFamily = InterFontFamily,
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = "Profile Visibility",
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
                    PrivacyToggleItem(
                        title = "Show Online Status",
                        subtitle = "Let other users see when you are active on Soulmate",
                        checked = showOnline,
                        onCheckedChange = { showOnline = it }
                    )
                    HorizontalDivider(color = Color(0xFF000000).copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 12.dp))
                    PrivacyToggleItem(
                        title = "Show Distance",
                        subtitle = "Display approximate distance in kilometers on your profile",
                        checked = showDistance,
                        onCheckedChange = { showDistance = it }
                    )
                    HorizontalDivider(color = Color(0xFF000000).copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 12.dp))
                    PrivacyToggleItem(
                        title = "Show Age",
                        subtitle = "Display your age badge next to your profile name",
                        checked = showAge,
                        onCheckedChange = { showAge = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Messaging Privacy",
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
                    PrivacyToggleItem(
                        title = "Messages from Non-Friends",
                        subtitle = "Allow people you haven't matched with to send introduction notes",
                        checked = allowNonFriends,
                        onCheckedChange = { allowNonFriends = it }
                    )
                    HorizontalDivider(color = Color(0xFF000000).copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 12.dp))
                    PrivacyToggleItem(
                        title = "Read Receipts",
                        subtitle = "Show double blue checkmarks when you have read messages",
                        checked = sendReadReceipts,
                        onCheckedChange = { sendReadReceipts = it }
                    )
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
                text = "Save Privacy Settings",
                onClick = {
                    onSave(
                        PrivacySettingsState(
                            showOnlineStatus = showOnline,
                            showDistance = showDistance,
                            showAge = showAge,
                            allowNonFriendsMessaging = allowNonFriends,
                            sendReadReceipts = sendReadReceipts
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PrivacyToggleItem(
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
