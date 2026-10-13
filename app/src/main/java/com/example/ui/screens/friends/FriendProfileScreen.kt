package com.example.ui.screens.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.User
import com.example.ui.components.GradientButton
import com.example.ui.screens.explore.FullProfileScreen
import com.example.ui.theme.AppBackground

@Composable
fun FriendProfileScreen(
    user: User,
    onRemoveFriend: () -> Unit,
    onSendMessage: () -> Unit,
    onBack: () -> Unit
) {
    FullProfileScreen(
        user = user,
        onBack = onBack,
        customBottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppBackground)
                    .border(width = 1.dp, color = Color(0xFF000000).copy(alpha = 0.08f))
                    .padding(horizontal = 24.dp, vertical = 14.dp)
                    .testTag("friend_profile_bottom_bar")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Remove circle button (red)
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.dp, Color(0xFFEF4444), CircleShape)
                            .clickable { onRemoveFriend() }
                            .testTag("remove_friend_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonRemove,
                            contentDescription = "Remove Friend",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Message gradient pill
                    GradientButton(
                        text = "Send Message",
                        onClick = onSendMessage,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.ChatBubble,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    )
}
