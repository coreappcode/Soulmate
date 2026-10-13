package com.example.ui.screens.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.navigation.Routes
import com.example.ui.components.EmptyState
import com.example.ui.components.SoulBottomNav
import com.example.ui.components.SoulTopBar
import com.example.ui.theme.AppBackground
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.H2Style
import com.example.ui.theme.H3Style
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.OrchidPrimary
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DashboardScreen(
    initialTab: String = Routes.DASHBOARD_EXPLORE
) {
    var currentTab by remember { mutableStateOf(initialTab) }

    Scaffold(
        bottomBar = {
            SoulBottomNav(
                currentRoute = currentTab,
                onNavigate = { newRoute -> currentTab = newRoute }
            )
        },
        containerColor = AppBackground,
        modifier = Modifier.fillMaxSize().testTag("dashboard_screen")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    (slideInHorizontally { width -> width / 3 } + fadeIn()) togetherWith
                        (slideOutHorizontally { width -> -width / 3 } + fadeOut())
                },
                label = "dashboard_tab_transition"
            ) { tab ->
                when (tab) {
                    Routes.DASHBOARD_CHATS -> ChatsPlaceholder()
                    Routes.DASHBOARD_EXPLORE -> ExplorePlaceholder()
                    Routes.DASHBOARD_FRIENDS -> FriendsPlaceholder()
                    Routes.DASHBOARD_SETTINGS -> SettingsPlaceholder()
                    Routes.DASHBOARD_PROFILE -> ProfilePlaceholder()
                    else -> ExplorePlaceholder()
                }
            }
        }
    }
}

@Composable
private fun ChatsPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        SoulTopBar(
            title = "Chats",
            showLogo = true,
            actions = {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Chats",
                        tint = TextPrimary
                    )
                }
            }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            PlaceholderCard(
                icon = Icons.Default.ChatBubble,
                tabName = "Chats",
                tagline = "Real-time conversation threads & match chats"
            )
        }
    }
}

@Composable
private fun ExplorePlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        SoulTopBar(
            title = "Explore",
            showLogo = true,
            actions = {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter Cards",
                        tint = TextPrimary
                    )
                }
            }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            PlaceholderCard(
                icon = Icons.Default.AutoAwesome,
                tabName = "Explore",
                tagline = "Interactive card swipe deck & discover radar"
            )
        }
    }
}

@Composable
private fun FriendsPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        SoulTopBar(
            title = "Friends",
            showLogo = true,
            actions = {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "Add Friends",
                        tint = TextPrimary
                    )
                }
            }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            PlaceholderCard(
                icon = Icons.Default.People,
                tabName = "Friends",
                tagline = "Mutual matches, soulmate list & social requests"
            )
        }
    }
}

@Composable
private fun SettingsPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        SoulTopBar(
            title = "Settings",
            showLogo = true,
            actions = {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = "Help Center",
                        tint = TextPrimary
                    )
                }
            }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            PlaceholderCard(
                icon = Icons.Default.Settings,
                tabName = "Settings",
                tagline = "Discovery preferences, privacy & account controls"
            )
        }
    }
}

@Composable
private fun ProfilePlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        SoulTopBar(
            title = "Profile",
            showLogo = true,
            actions = {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        tint = TextPrimary
                    )
                }
            }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            PlaceholderCard(
                icon = Icons.Default.Person,
                tabName = "Profile",
                tagline = "Public preview, verification status & photo manager"
            )
        }
    }
}

@Composable
private fun PlaceholderCard(
    icon: ImageVector,
    tabName: String,
    tagline: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(ElevatedSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = tabName,
                tint = OrchidPrimary,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "$tabName — Coming in Prompt 2/3",
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = tagline,
            fontFamily = InterFontFamily,
            fontSize = 14.sp,
            color = TextMuted,
            textAlign = TextAlign.Center
        )
    }
}
