package com.example.ui.screens.explore

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
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.User
import com.example.ui.components.SoulAvatar
import com.example.ui.components.SoulSearchBar
import com.example.ui.components.SoulTopBar
import com.example.ui.theme.AppBackground
import com.example.ui.theme.AuroraHorizontalGradient
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.OrchidPrimary
import com.example.ui.theme.PillShape
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun UsernameSearchScreen(
    results: List<User>,
    sentRequestIds: Set<String>,
    onSearchQuery: (String) -> Unit,
    onUserClick: (User) -> Unit,
    onAddFriend: (User) -> Unit,
    onBack: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var debouncedQuery by remember { mutableStateOf("") }

    // Debounced 400ms filter
    LaunchedEffect(query) {
        kotlinx.coroutines.delay(400)
        debouncedQuery = query
    }
    LaunchedEffect(debouncedQuery) {
        onSearchQuery(debouncedQuery)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("username_search_screen")
    ) {
        SoulTopBar(
            title = "Search People",
            showBack = true,
            onBackClick = onBack
        )

        // Sticky search bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppBackground)
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            SoulSearchBar(
                value = query,
                onValueChange = { query = it },
                placeholder = "Search by username e.g. aarav_s..."
            )
        }

        if (results.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(ElevatedSurface)
                        .border(1.5.dp, Color(0xFF000000), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = OrchidPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (query.isBlank()) "Search people by username" else "No soulmates found",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (query.isBlank()) {
                        "Enter a username or name to search real profiles."
                    } else {
                        "We couldn't find anyone matching \"$query\". Check spelling or try a different handle."
                    },
                    fontFamily = InterFontFamily,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(results, key = { it.id }) { user ->
                    val isAdded = user.id in sentRequestIds
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, Color(0xFF000000), RoundedCornerShape(18.dp))
                            .clickable { onUserClick(user) }
                            .testTag("search_result_${user.username}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
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
                                    text = user.name,
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.SemiBold,
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
                                        if (isAdded) {
                                            Modifier
                                                .background(Color(0xFFE5E7EB))
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        } else {
                                            Modifier
                                                .background(AuroraHorizontalGradient)
                                                .clickable {
                                                    onAddFriend(user)
                                                }
                                                .padding(horizontal = 14.dp, vertical = 7.dp)
                                        }
                                    )
                                    .testTag("add_user_${user.username}")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isAdded) Icons.Default.Check else Icons.Default.PersonAdd,
                                        contentDescription = null,
                                        tint = if (isAdded) TextSecondary else Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isAdded) "Added" else "Add",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isAdded) TextSecondary else Color.White
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
