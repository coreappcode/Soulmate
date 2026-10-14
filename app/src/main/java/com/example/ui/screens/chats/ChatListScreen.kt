package com.example.ui.screens.chats

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatConversation
import com.example.data.MockData
import com.example.data.User
import com.example.ui.components.GradientButton
import com.example.ui.components.SoulAvatar
import com.example.ui.components.SoulSearchBar
import com.example.ui.components.SoulTopBar
import com.example.ui.screens.system.CardSkeleton
import com.example.ui.theme.AppBackground
import com.example.ui.theme.AuroraHorizontalGradient
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.HairlineDivider
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.OrchidPrimary
import com.example.ui.theme.PillShape
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.RosePrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatListScreen(
    conversations: List<ChatConversation>,
    onSelectConversation: (ChatConversation) -> Unit,
    onRequireVerifyToUnlock: (User) -> Unit,
    onLongPressConversation: (ChatConversation) -> Unit,
    onNewChatClick: () -> Unit,
    onGoToExplore: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(600)
        isLoading = false
    }

    val filteredConversations = conversations.filter {
        it.user.name.contains(searchQuery, ignoreCase = true) ||
            it.user.username.contains(searchQuery, ignoreCase = true) ||
            it.lastMessage.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("chat_list_screen")
    ) {
        // Top bar (#FBE9F1): "Chats" + search icon + new-chat icon
        SoulTopBar(
            title = "Chats",
            showBack = false,
            actions = {
                IconButton(
                    onClick = onNewChatClick,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("new_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "New Chat",
                        tint = Color.White
                    )
                }
            }
        )

        // Sticky SoulSearchBar below
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppBackground)
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            SoulSearchBar(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Search matches & conversations..."
            )
        }

        if (isLoading) {
            Column(modifier = Modifier.padding(20.dp)) {
                repeat(5) {
                    CardSkeleton(modifier = Modifier.padding(vertical = 6.dp))
                }
            }
        } else if (filteredConversations.isEmpty()) {
            // Empty state: illustration + "No chats yet" + Go to Explore CTA
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(ElevatedSurface)
                            .border(1.5.dp, Color(0xFF000000), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            tint = OrchidPrimary,
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "No chats yet",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Swipe right on soulmates to start conversations and make sparks fly!",
                        fontFamily = InterFontFamily,
                        fontSize = 14.sp,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    GradientButton(
                        text = "Go to Explore",
                        onClick = onGoToExplore,
                        modifier = Modifier.fillMaxWidth(0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                // Online Matches row
                item {
                    Column(modifier = Modifier.padding(start = 20.dp, top = 8.dp, bottom = 12.dp)) {
                        Text(
                            text = "New Matches",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(MockData.users.take(7)) { user ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .width(64.dp)
                                        .clickable {
                                            if (user.verified) {
                                                val conv = conversations.firstOrNull { it.user.id == user.id }
                                                    ?: ChatConversation("c_${user.id}", user, "Hi!", "Now")
                                                onSelectConversation(conv)
                                            } else {
                                                onRequireVerifyToUnlock(user)
                                            }
                                        }
                                ) {
                                    SoulAvatar(
                                        imageUrl = user.photos.firstOrNull() ?: "",
                                        size = 56.dp,
                                        showOnlineDot = user.isOnline,
                                        isVerified = user.verified
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = user.name.split(" ").first(),
                                        fontFamily = InterFontFamily,
                                        fontSize = 12.sp,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(
                        color = Color(0xFF000000).copy(alpha = 0.06f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                // Chat rows: SoulAvatar (56dp, online dot) + name + last message (1 line ellipsis) + time + unread gradient badge
                items(filteredConversations, key = { it.id }) { conv ->
                    val user = conv.user
                    val isVerified = user.verified

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    if (isVerified) {
                                        onSelectConversation(conv)
                                    } else {
                                        onRequireVerifyToUnlock(user)
                                    }
                                },
                                onLongClick = {
                                    onLongPressConversation(conv)
                                }
                            )
                            .testTag("chat_row_${user.username}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SoulAvatar(
                                imageUrl = user.photos.firstOrNull() ?: "",
                                size = 56.dp,
                                showOnlineDot = user.isOnline,
                                isVerified = isVerified
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = user.name,
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        color = TextPrimary
                                    )

                                    Text(
                                        text = conv.timestamp,
                                        fontFamily = InterFontFamily,
                                        fontSize = 11.sp,
                                        color = if (conv.unreadCount > 0) OrchidPrimary else TextMuted
                                    )
                                }

                                Spacer(modifier = Modifier.height(3.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (conv.isTyping) "typing..." else conv.lastMessage,
                                        fontFamily = InterFontFamily,
                                        fontWeight = if (conv.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                                        fontSize = 13.sp,
                                        color = if (conv.isTyping) OrchidPrimary else TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                                    )

                                    if (conv.unreadCount > 0) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(AuroraHorizontalGradient),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${conv.unreadCount}",
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Divider inset left 88dp
                        HorizontalDivider(
                            color = HairlineDivider,
                            thickness = 1.dp,
                            modifier = Modifier.padding(start = 88.dp)
                        )
                    }
                }
            }
        }
    }
}
