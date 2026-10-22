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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.User
import com.example.ui.components.SoulAvatar
import com.example.ui.components.SoulSearchBar
import com.example.ui.components.SoulTopBar
import com.example.ui.theme.AppBackground
import com.example.ui.theme.AuroraHorizontalGradient
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.OrchidPrimary
import com.example.ui.theme.PillShape
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun FindFriendsScreen(
    candidates: List<User> = emptyList(),
    sentRequestIds: Set<String> = emptySet(),
    onConnect: (User) -> Unit,
    onOpenProfile: (User) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filtered = candidates.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
            it.username.contains(searchQuery, ignoreCase = true) ||
            it.bio.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("find_friends_screen")
    ) {
        SoulTopBar(
            title = "Find Friends",
            showBack = true,
            onBackClick = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            SoulSearchBar(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Search people by interests, city...",
                modifier = Modifier.padding(bottom = 14.dp)
            )

            if (filtered.isEmpty()) {
                FriendsEmptyState(
                    title = "No suggestions found",
                    description = "Try searching for a different name, city, or interest.",
                    actionText = "Clear Search",
                    onAction = { searchQuery = "" }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtered, key = { it.id }) { user ->
                        val isConnected = user.id in sentRequestIds
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.5.dp, Color(0xFF000000), RoundedCornerShape(20.dp))
                                .clickable { onOpenProfile(user) }
                                .testTag("suggestion_card_${user.username}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    SoulAvatar(
                                        imageUrl = user.photos.firstOrNull() ?: "",
                                        size = 54.dp,
                                        showOnlineDot = user.isOnline,
                                        isVerified = user.verified
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = user.nameWithAge,
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "@${user.username} • ${user.distance}",
                                            fontFamily = InterFontFamily,
                                            fontSize = 12.sp,
                                            color = OrchidPrimary
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(PillShape)
                                            .then(
                                                if (isConnected) {
                                                    Modifier
                                                        .background(Color(0xFFE5E7EB))
                                                        .padding(horizontal = 14.dp, vertical = 7.dp)
                                                } else {
                                                    Modifier
                                                        .background(AuroraHorizontalGradient)
                                                        .clickable {
                                                            onConnect(user)
                                                        }
                                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                                }
                                            )
                                            .testTag("connect_btn_${user.username}")
                                    ) {
                                        Text(
                                            text = if (isConnected) "Requested" else "Connect",
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isConnected) TextSecondary else Color.White
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = user.bio,
                                    fontFamily = InterFontFamily,
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
