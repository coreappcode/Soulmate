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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProfileState
import com.example.ui.NotificationSettingsState
import com.example.ui.components.SoulAvatar
import com.example.ui.components.SoulTopBar
import com.example.ui.theme.AppBackground
import com.example.ui.theme.AuroraHorizontalGradient
import com.example.ui.theme.CardBackground
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.OrchidPrimary
import com.example.ui.theme.PillShape
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.RosePrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    userProfile: UserProfileState,
    notificationSettings: NotificationSettingsState,
    onTogglePush: (Boolean) -> Unit,
    onToggleEmail: (Boolean) -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToVerificationStatus: () -> Unit,
    onNavigateToPhoneNumber: () -> Unit,
    onNavigateToMatchmaking: () -> Unit,
    onNavigateToLocation: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToBlockedUsers: () -> Unit,
    onNavigateToProPlans: () -> Unit,
    onNavigateToTerms: () -> Unit,
    onNavigateToCommunityRules: () -> Unit,
    onNavigateToAccount: () -> Unit,
    onNavigateToLogoutConfirm: () -> Unit,
    onNavigateToDeleteAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("settings_screen")
    ) {
        // Top bar (#FBE9F1): "Settings"
        SoulTopBar(
            title = "Settings",
            showBack = false
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Profile Summary Card (Avatar 72dp + name + @username + verified + Edit Profile small gradient pill)
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Color(0xFF000000), RoundedCornerShape(22.dp))
                    .testTag("profile_summary_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SoulAvatar(
                        imageUrl = userProfile.photos.firstOrNull() ?: "",
                        size = 72.dp,
                        showOnlineDot = true,
                        isVerified = userProfile.isFaceVerified
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userProfile.fullName.ifBlank { "Anya Sharma" },
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "@${userProfile.username.ifBlank { "anya_sharma" }}",
                            fontFamily = InterFontFamily,
                            fontSize = 13.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(AuroraHorizontalGradient)
                                .clickable { onNavigateToEditProfile() }
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                                .testTag("edit_profile_pill")
                        ) {
                            Text(
                                text = "Edit Profile",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION: ACCOUNT
            SectionHeader(title = "ACCOUNT")
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color(0xFF000000), RoundedCornerShape(20.dp))
            ) {
                Column {
                    SettingsRow(icon = Icons.Default.Person, title = "Account Settings", onClick = onNavigateToAccount)
                    SettingsDivider()
                    SettingsRow(icon = Icons.Default.VerifiedUser, title = "Verification Status", onClick = onNavigateToVerificationStatus)
                    SettingsDivider()
                    SettingsRow(icon = Icons.Default.Phone, title = "Phone Number", subtitle = userProfile.formattedPhoneNumber, onClick = onNavigateToPhoneNumber)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION: DISCOVERY
            SectionHeader(title = "DISCOVERY")
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color(0xFF000000), RoundedCornerShape(20.dp))
            ) {
                Column {
                    SettingsRow(icon = Icons.Default.Tune, title = "Matchmaking Preferences", onClick = onNavigateToMatchmaking)
                    SettingsDivider()
                    SettingsRow(icon = Icons.Default.LocationOn, title = "Location", subtitle = "${userProfile.city}, ${userProfile.selectedCountry.name}", onClick = onNavigateToLocation)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION: PRIVACY
            SectionHeader(title = "PRIVACY")
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color(0xFF000000), RoundedCornerShape(20.dp))
            ) {
                Column {
                    SettingsRow(icon = Icons.Default.Lock, title = "Privacy & Visibility", onClick = onNavigateToPrivacy)
                    SettingsDivider()
                    SettingsRow(icon = Icons.Default.Block, title = "Blocked Users", onClick = onNavigateToBlockedUsers)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION: NOTIFICATIONS
            SectionHeader(title = "NOTIFICATIONS")
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color(0xFF000000), RoundedCornerShape(20.dp))
            ) {
                Column {
                    SettingsToggleRow(
                        icon = Icons.Default.Notifications,
                        title = "Push Notifications",
                        checked = notificationSettings.pushMatches || notificationSettings.pushMessages,
                        onCheckedChange = onTogglePush
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Default.Email,
                        title = "Email Notifications",
                        checked = notificationSettings.emailSummaries,
                        onCheckedChange = onToggleEmail
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION: SUBSCRIPTION
            SectionHeader(title = "SUBSCRIPTION")
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color(0xFF000000), RoundedCornerShape(20.dp))
                    .clickable { onNavigateToProPlans() }
                    .testTag("soulmate_pro_settings_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ElevatedSurface)
                            .border(1.dp, OrchidPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = OrchidPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Soulmate Pro",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Unlimited rewinds, see who likes you & more",
                            fontFamily = InterFontFamily,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(AuroraHorizontalGradient)
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Upgrade",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION: ABOUT
            SectionHeader(title = "ABOUT")
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color(0xFF000000), RoundedCornerShape(20.dp))
            ) {
                Column {
                    SettingsRow(icon = Icons.Default.Description, title = "Terms of Service", onClick = onNavigateToTerms)
                    SettingsDivider()
                    SettingsRow(icon = Icons.Default.Gavel, title = "Community Rules", onClick = onNavigateToCommunityRules)
                    SettingsDivider()
                    // App Version v1.0.0 (no chevron)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(ElevatedSurface)
                                    .border(1.dp, Color(0xFF000000), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = "App Version",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "v1.0.0",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION: DANGER ZONE (red tint card)
            SectionHeader(title = "DANGER ZONE")
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color(0xFFEF4444), RoundedCornerShape(20.dp))
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Default.Logout,
                        title = "Log Out",
                        tintColor = Color(0xFFDC2626),
                        onClick = onNavigateToLogoutConfirm
                    )
                    SettingsDivider(color = Color(0xFFFCA5A5))
                    SettingsRow(
                        icon = Icons.Default.DeleteForever,
                        title = "Delete Account",
                        tintColor = Color(0xFFDC2626),
                        onClick = onNavigateToDeleteAccount
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        color = TextSecondary,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    tintColor: Color = OrchidPrimary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("settings_row_${title.lowercase().replace(" ", "_")}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(ElevatedSurface)
                .border(1.dp, Color(0xFF000000), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tintColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = if (tintColor == Color(0xFFDC2626)) tintColor else TextPrimary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontFamily = InterFontFamily,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(ElevatedSurface)
                .border(1.dp, Color(0xFF000000), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = OrchidPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = title,
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = OrchidPrimary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = ElevatedSurface
            )
        )
    }
}

@Composable
private fun SettingsDivider(color: Color = Color(0xFF000000).copy(alpha = 0.08f)) {
    HorizontalDivider(
        thickness = 1.dp,
        color = color,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}
