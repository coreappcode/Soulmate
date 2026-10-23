package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.navigation.Routes
import com.example.ui.theme.AppBackground
import com.example.ui.theme.BrandShadowSoft
import com.example.ui.theme.CardBackground
import com.example.ui.theme.OrchidPrimary
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.RosePrimary
import com.example.ui.theme.SemanticError
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawWithCache
import com.example.ui.theme.ActiveTabLabel
import com.example.ui.theme.AuroraHorizontalGradient
import com.example.ui.theme.BottomNavBackground
import com.example.ui.theme.BrandShadowColor
import com.example.ui.theme.HairlineDivider
import com.example.ui.theme.HeaderBackground
import com.example.ui.theme.InactiveTabColor
import com.example.ui.theme.TinderPink

fun Modifier.gradientTint(brush: Brush): Modifier = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithCache {
        onDrawWithContent {
            drawContent()
            drawRect(brush = brush, blendMode = BlendMode.SrcAtop)
        }
    }

@Composable
fun SoulTopBar(
    title: String = "",
    showBack: Boolean = false,
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    containerColor: Color = HeaderBackground,
    contentColor: Color = Color.White,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(containerColor)
            .statusBarsPadding()
            .height(56.dp)
            .drawBehind {
                drawLine(
                    color = Color.White.copy(alpha = 0.25f),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .padding(horizontal = 16.dp)
            .testTag("top_bar_${title.lowercase().replace(" ", "_")}")
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides contentColor
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showBack) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("top_bar_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = contentColor
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    if (title.isNotEmpty()) {
                        Text(
                            text = title,
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = contentColor
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    content = actions
                )
            }
        }
    }
}

private data class NavItemData(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeCount: String? = null
)

@Composable
fun SoulBottomNav(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    chatUnreadCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavItemData(
            route = Routes.DASHBOARD_EXPLORE,
            label = "Explore",
            selectedIcon = Icons.Filled.AutoAwesome,
            unselectedIcon = Icons.Outlined.AutoAwesome
        ),
        NavItemData(
            route = Routes.DASHBOARD_CHATS,
            label = "Chats",
            selectedIcon = Icons.Filled.ChatBubble,
            unselectedIcon = Icons.Outlined.ChatBubbleOutline,
            badgeCount = chatUnreadCount.takeIf { it > 0 }?.let { count ->
                if (count > 99) "99+" else count.toString()
            }
        ),
        NavItemData(
            route = Routes.DASHBOARD_FRIENDS,
            label = "Friends",
            selectedIcon = Icons.Filled.People,
            unselectedIcon = Icons.Outlined.PeopleOutline,
            badgeCount = "2"
        ),
        NavItemData(
            route = Routes.DASHBOARD_SETTINGS,
            label = "Settings",
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings
        ),
        NavItemData(
            route = Routes.DASHBOARD_PROFILE,
            label = "Profile",
            selectedIcon = Icons.Filled.Person,
            unselectedIcon = Icons.Outlined.PersonOutline
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                ambientColor = BrandShadowColor,
                spotColor = BrandShadowColor
            )
            .background(BottomNavBackground)
            .drawBehind {
                drawLine(
                    color = Color.White.copy(alpha = 0.25f),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .navigationBarsPadding()
            .height(68.dp)
            .padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier.matchParentSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .clickable { onNavigate(item.route) }
                        .padding(vertical = 4.dp)
                        .testTag("nav_tab_${item.label.lowercase()}"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    BadgedBox(
                        badge = {
                            if (item.badgeCount != null) {
                                Badge(
                                    containerColor = Color.White,
                                    contentColor = TinderPink
                                ) {
                                    Text(
                                        text = item.badgeCount,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TinderPink
                                    )
                                }
                            }
                        }
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = item.selectedIcon,
                                contentDescription = item.label,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Icon(
                                imageVector = item.unselectedIcon,
                                contentDescription = item.label,
                                tint = Color.White.copy(alpha = 0.65f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = item.label,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 11.sp,
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.70f)
                    )
                }
            }
        }
    }
}
