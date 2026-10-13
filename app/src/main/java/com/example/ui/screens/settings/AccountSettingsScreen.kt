package com.example.ui.screens.settings

import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.UserProfileState
import com.example.ui.components.SoulTopBar
import com.example.ui.theme.AppBackground
import com.example.ui.theme.CardBackground
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.OrchidPrimary
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AccountSettingsScreen(
    userProfile: UserProfileState,
    onChangePhone: () -> Unit,
    onChangeUsername: () -> Unit,
    onChangeEmail: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("account_settings_screen")
    ) {
        SoulTopBar(
            title = "Account Settings",
            showBack = true,
            onBackClick = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Text(
                text = "Contact & Credentials",
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
                Column {
                    AccountOptionRow(
                        icon = Icons.Default.Phone,
                        title = "Phone Number",
                        value = "${userProfile.selectedCountry.dialCode} ${userProfile.phoneNumber}",
                        onClick = onChangePhone
                    )
                    HorizontalDivider(color = Color(0xFF000000).copy(alpha = 0.08f), modifier = Modifier.padding(horizontal = 16.dp))
                    AccountOptionRow(
                        icon = Icons.Default.Person,
                        title = "Username",
                        value = "@${userProfile.username}",
                        onClick = onChangeUsername
                    )
                    HorizontalDivider(color = Color(0xFF000000).copy(alpha = 0.08f), modifier = Modifier.padding(horizontal = 16.dp))
                    AccountOptionRow(
                        icon = Icons.Default.Email,
                        title = "Email Address",
                        value = userProfile.email,
                        onClick = onChangeEmail
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Data & Account Lifecycle",
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
                Column {
                    AccountOptionRow(
                        icon = Icons.Default.CloudDownload,
                        title = "Download My Data",
                        value = "Request JSON archive",
                        onClick = {
                            Toast.makeText(context, "Your data archive link will be sent to ${userProfile.email}", Toast.LENGTH_LONG).show()
                        }
                    )
                    HorizontalDivider(color = Color(0xFF000000).copy(alpha = 0.08f), modifier = Modifier.padding(horizontal = 16.dp))
                    AccountOptionRow(
                        icon = Icons.Default.PauseCircle,
                        title = "Deactivate Account",
                        value = "Pause profile visibility",
                        onClick = {
                            Toast.makeText(context, "Account paused. You won't be shown to new users.", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountOptionRow(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
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

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = TextPrimary
            )
            Text(
                text = value,
                fontFamily = InterFontFamily,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}
