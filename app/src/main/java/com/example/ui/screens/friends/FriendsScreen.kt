package com.example.ui.screens.friends

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FriendRequestItem
import com.example.data.SentFriendRequest
import com.example.data.User
import com.example.ui.components.GradientButton
import com.example.ui.components.SegmentedControl
import com.example.ui.components.SoulAvatar
import com.example.ui.components.SoulCard
import com.example.ui.components.SoulSearchBar
import com.example.ui.components.SoulTextButton
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
import com.example.ui.theme.SemanticOnline
import com.example.ui.theme.SemanticWarning
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun FriendsScreen(
    friendsList: List<User>,
    friendRequests: List<FriendRequestItem>,
    sentRequests: List<SentFriendRequest>,
    onAcceptRequest: (FriendRequestItem) -> Unit,
    onDeclineRequest: (FriendRequestItem) -> Unit,
    onCancelSentRequest: (SentFriendRequest) -> Unit,
    onOpenRequestDetail: (FriendRequestItem) -> Unit,
    onOpenFriendProfile: (User) -> Unit,
    onOpenChat: (User) -> Unit,
    onOpenFindFriends: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: All, 1: Requests, 2: Sent
    var searchQuery by remember { mutableStateOf("") }
    var showSearchBar by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(600)
        isLoading = false
    }

    val tabOptions = listOf(
        "All",
        "Requests (${friendRequests.size})",
        "Sent (${sentRequests.size})"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("friends_screen")
    ) {
        // Top bar (#FBE9F1): "Friends" + search icon
        SoulTopBar(
            title = "Friends",
            showBack = false,
            actions = {
                IconButton(
                    onClick = { showSearchBar = !showSearchBar },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("friends_search_toggle")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = onOpenFindFriends,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("find_friends_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "Find Friends",
                        tint = Color.White
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            if (showSearchBar) {
                SoulSearchBar(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "Filter by name or username...",
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // SegmentedControl: [All] [Requests (count)] [Sent (count)]
            SegmentedControl(
                options = tabOptions,
                selectedIndex = selectedTab,
                onSelect = { selectedTab = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (isLoading) {
                repeat(4) {
                    CardSkeleton(modifier = Modifier.padding(vertical = 6.dp))
                }
            } else {
                when (selectedTab) {
                    0 -> {
                        // All Friends tab
                        val filtered = friendsList.filter {
                            it.name.contains(searchQuery, ignoreCase = true) ||
                                it.username.contains(searchQuery, ignoreCase = true)
                        }

                        if (filtered.isEmpty()) {
                            FriendsEmptyState(
                                title = "No friends yet",
                                description = "Explore nearby matches and connect with soulmates who share your passions.",
                                actionText = "Find Friends",
                                onAction = onOpenFindFriends
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(filtered, key = { it.id }) { friend ->
                                    FriendRow(
                                        user = friend,
                                        onRowClick = { onOpenFriendProfile(friend) },
                                        onMessageClick = { onOpenChat(friend) }
                                    )
                                }
                            }
                        }
                    }

                    1 -> {
                        // Requests tab
                        val filtered = friendRequests.filter {
                            it.user.name.contains(searchQuery, ignoreCase = true) ||
                                it.user.username.contains(searchQuery, ignoreCase = true)
                        }

                        if (filtered.isEmpty()) {
                            FriendsEmptyState(
                                title = "No incoming requests",
                                description = "When someone wants to be your friend, you'll see their request here.",
                                actionText = "Discover Soulmates",
                                onAction = onOpenFindFriends
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(filtered, key = { it.id }) { request ->
                                    RequestRow(
                                        request = request,
                                        onRowClick = { onOpenRequestDetail(request) },
                                        onAccept = { onAcceptRequest(request) },
                                        onDecline = { onDeclineRequest(request) }
                                    )
                                }
                            }
                        }
                    }

                    2 -> {
                        // Sent Requests tab
                        val filtered = sentRequests.filter {
                            it.user.name.contains(searchQuery, ignoreCase = true) ||
                                it.user.username.contains(searchQuery, ignoreCase = true)
                        }

                        if (filtered.isEmpty()) {
                            FriendsEmptyState(
                                title = "No pending sent requests",
                                description = "Send requests from Explore or Find Friends to start growing your circle.",
                                actionText = "Browse People",
                                onAction = onOpenFindFriends
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(filtered, key = { it.id }) { sent ->
                                    SentRequestRow(
                                        sent = sent,
                                        onCancel = { onCancelSentRequest(sent) },
                                        onRowClick = { onOpenFriendProfile(sent.user) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendRow(
    user: User,
    onRowClick: () -> Unit,
    onMessageClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, Color(0xFF000000), RoundedCornerShape(18.dp))
            .clickable { onRowClick() }
            .testTag("friend_row_${user.username}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SoulAvatar(
                imageUrl = user.photos.firstOrNull() ?: "",
                size = 52.dp,
                showOnlineDot = user.isOnline,
                isVerified = user.verified
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${user.name}, ${user.age}",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
                Text(
                    text = "@${user.username}",
                    fontFamily = InterFontFamily,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Box(
                modifier = Modifier
                    .clip(PillShape)
                    .background(AuroraHorizontalGradient)
                    .clickable { onMessageClick() }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("message_friend_${user.username}")
            ) {
                Text(
                    text = "Message",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun RequestRow(
    request: FriendRequestItem,
    onRowClick: () -> Unit,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, Color(0xFF000000), RoundedCornerShape(18.dp))
            .clickable { onRowClick() }
            .testTag("request_row_${request.user.username}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SoulAvatar(
                imageUrl = request.user.photos.firstOrNull() ?: "",
                size = 52.dp,
                showOnlineDot = true,
                isVerified = request.user.verified
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = request.user.name,
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
                Text(
                    text = "${request.mutualInterests} mutual interests",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = OrchidPrimary
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Decline circle button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ElevatedSurface)
                        .border(1.dp, Color(0xFF000000), CircleShape)
                        .clickable { onDecline() }
                        .testTag("decline_request_${request.user.username}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Decline",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Accept gradient pill
                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(AuroraHorizontalGradient)
                        .clickable { onAccept() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("accept_request_${request.user.username}")
                ) {
                    Text(
                        text = "Accept",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun SentRequestRow(
    sent: SentFriendRequest,
    onCancel: () -> Unit,
    onRowClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, Color(0xFF000000), RoundedCornerShape(18.dp))
            .clickable { onRowClick() }
            .testTag("sent_row_${sent.user.username}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SoulAvatar(
                imageUrl = sent.user.photos.firstOrNull() ?: "",
                size = 52.dp,
                isVerified = sent.user.verified
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sent.user.name,
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(Color(0xFFFEF3C7))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Pending",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = Color(0xFFD97706)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = sent.timestamp,
                        fontFamily = InterFontFamily,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(PillShape)
                    .background(ElevatedSurface)
                    .border(1.dp, Color(0xFF000000), PillShape)
                    .clickable { onCancel() }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
                    .testTag("cancel_sent_${sent.user.username}")
            ) {
                Text(
                    text = "Cancel",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun FriendsEmptyState(
    title: String,
    description: String,
    actionText: String,
    onAction: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(ElevatedSurface)
                .border(1.5.dp, Color(0xFF000000), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.People,
                contentDescription = null,
                tint = OrchidPrimary,
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = title,
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            fontFamily = InterFontFamily,
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        GradientButton(
            text = actionText,
            onClick = onAction,
            modifier = Modifier.fillMaxWidth(0.75f)
        )
    }
}
