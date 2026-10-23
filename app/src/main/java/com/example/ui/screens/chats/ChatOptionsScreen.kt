package com.example.ui.screens.chats

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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import com.example.data.User
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
fun ChatOptionsScreen(
    user: User,
    onViewProfile: () -> Unit,
    onMediaAndFiles: () -> Unit,
    onMute: () -> Unit,
    onClearChat: () -> Unit,
    onBlockUser: () -> Unit,
    onReportUser: () -> Unit,
    onDeleteChat: () -> Unit,
    onBack: () -> Unit
) {
    var pendingDestructiveAction by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("chat_options_screen")
    ) {
        SoulTopBar(
            title = "Chat Options",
            showBack = true,
            onBackClick = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Text(
                text = "Options for ${user.name}",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // General options card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color(0xFF000000), RoundedCornerShape(20.dp))
            ) {
                Column {
                    OptionRow(icon = Icons.Default.Person, title = "View Profile", onClick = onViewProfile)
                    HorizontalDivider(color = Color(0xFF000000).copy(alpha = 0.08f), modifier = Modifier.padding(horizontal = 16.dp))
                    OptionRow(icon = Icons.Default.Folder, title = "Chat Info", onClick = onMediaAndFiles)
                    HorizontalDivider(color = Color(0xFF000000).copy(alpha = 0.08f), modifier = Modifier.padding(horizontal = 16.dp))
                    OptionRow(icon = Icons.Default.NotificationsOff, title = "Mute Notifications", onClick = onMute)
                    HorizontalDivider(color = Color(0xFF000000).copy(alpha = 0.08f), modifier = Modifier.padding(horizontal = 16.dp))
                    OptionRow(
                        icon = Icons.Default.RemoveCircleOutline,
                        title = "Clear Chat History",
                        onClick = { pendingDestructiveAction = "clear" }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Danger actions card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color(0xFFEF4444), RoundedCornerShape(20.dp))
            ) {
                Column {
                    OptionRow(icon = Icons.Default.Block, title = "Block User", isDanger = true, onClick = onBlockUser)
                    HorizontalDivider(color = Color(0xFFFCA5A5), modifier = Modifier.padding(horizontal = 16.dp))
                    OptionRow(icon = Icons.Default.Flag, title = "Report User", isDanger = true, onClick = onReportUser)
                    HorizontalDivider(color = Color(0xFFFCA5A5), modifier = Modifier.padding(horizontal = 16.dp))
                    OptionRow(
                        icon = Icons.Default.Delete,
                        title = "Delete Chat",
                        isDanger = true,
                        onClick = { pendingDestructiveAction = "delete" }
                    )
                }
            }
        }
    }
    pendingDestructiveAction?.let { action ->
        val isClearAction = action == "clear"
        AlertDialog(
            onDismissRequest = { pendingDestructiveAction = null },
            title = {
                Text(if (isClearAction) "Clear chat history?" else "Delete chat?")
            },
            text = {
                Text(
                    if (isClearAction) {
                        "This clears messages from your view only. The other participant's history is unchanged."
                    } else {
                        "This removes the chat from your conversation list. The other participant's history is unchanged."
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDestructiveAction = null
                        if (isClearAction) onClearChat() else onDeleteChat()
                    }
                ) {
                    Text(if (isClearAction) "Clear" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDestructiveAction = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun OptionRow(
    icon: ImageVector,
    title: String,
    isDanger: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("chat_option_${title.lowercase().replace(" ", "_")}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (isDanger) Color(0xFFFEE2E2) else ElevatedSurface)
                .border(1.dp, if (isDanger) Color(0xFFEF4444) else Color(0xFF000000), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDanger) Color(0xFFDC2626) else OrchidPrimary,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = title,
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = if (isDanger) Color(0xFFDC2626) else TextPrimary,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = if (isDanger) Color(0xFFDC2626) else TextMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}
