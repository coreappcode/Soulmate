package com.example.ui.screens.explore

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.User
import com.example.ui.components.GradientButton
import com.example.ui.components.SegmentedControl
import com.example.ui.components.SoulTopBar
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.AppBackground
import com.example.ui.theme.AuroraHorizontalGradient
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.OrchidPrimary
import com.example.ui.theme.PillShape
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.RosePrimary
import com.example.ui.theme.SemanticOnline
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun ExploreScreen(
    candidates: List<User>,
    selectedSegment: Int,
    onSegmentChange: (Int) -> Unit,
    onLike: (User) -> Unit,
    onPass: (User) -> Unit,
    onRewind: () -> Unit,
    onCardClick: (User) -> Unit,
    onOpenFilters: () -> Unit,
    onOpenSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    val currentTopUser = candidates.firstOrNull()

    // Drag and animation state for top card
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    var hasVibratedThreshold by remember { mutableStateOf(false) }

    // Reset offsets when top candidate changes
    LaunchedEffect(currentTopUser?.id) {
        offsetX.snapTo(0f)
        offsetY.snapTo(0f)
        hasVibratedThreshold = false
    }

    val rotation = (offsetX.value / 20f).coerceIn(-15f, 15f)
    val likeAlpha = (offsetX.value / 150f).coerceIn(0f, 1f)
    val nopeAlpha = (-offsetX.value / 150f).coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("explore_screen")
    ) {
        // Top bar (#FBE9F1): "Explore" title + filter icon + search icon
        SoulTopBar(
            title = "Explore",
            showBack = false,
            actions = {
                IconButton(
                    onClick = onOpenSearch,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("explore_search_icon")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Users",
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = onOpenFilters,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("explore_filter_icon")
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = Color.White
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // SegmentedControl pill below bar: [Nearby] [Country] [Global]
            SegmentedControl(
                options = listOf("Nearby", "Country", "Global"),
                selectedIndex = selectedSegment,
                onSelect = onSegmentChange,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (candidates.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(ElevatedSurface)
                                .border(2.dp, Color(0xFF000000), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay,
                                contentDescription = null,
                                tint = OrchidPrimary,
                                modifier = Modifier.size(44.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "You've seen everyone nearby",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = TextPrimary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Expand your radius or adjust age preferences to discover more people in your region.",
                            fontFamily = InterFontFamily,
                            fontSize = 14.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        GradientButton(
                            text = "Widen Radius",
                            onClick = onOpenFilters,
                            modifier = Modifier.fillMaxWidth(0.75f)
                        )
                    }
                }
            } else {
                // Swipe Card Stack (3 cards layered, top interactive)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Card 2 (Bottom)
                    if (candidates.size > 2) {
                        SwipeCardItem(
                            user = candidates[2],
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(3f / 4.2f)
                                .offset(y = 20.dp)
                                .scale(0.90f)
                                .alpha(0.6f)
                        )
                    }

                    // Card 1 (Middle)
                    if (candidates.size > 1) {
                        SwipeCardItem(
                            user = candidates[1],
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(3f / 4.2f)
                                .offset(y = 10.dp)
                                .scale(0.95f)
                                .alpha(0.85f)
                        )
                    }

                    // Card 0 (Top - Interactive)
                    if (currentTopUser != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(3f / 4.2f)
                                .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
                                .rotate(rotation)
                                .pointerInput(currentTopUser.id) {
                                    detectDragGestures(
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            coroutineScope.launch {
                                                offsetX.snapTo(offsetX.value + dragAmount.x)
                                                offsetY.snapTo(offsetY.value + dragAmount.y)

                                                if (abs(offsetX.value) > 200f && !hasVibratedThreshold) {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    hasVibratedThreshold = true
                                                } else if (abs(offsetX.value) <= 200f) {
                                                    hasVibratedThreshold = false
                                                }
                                            }
                                        },
                                        onDragEnd = {
                                            coroutineScope.launch {
                                                if (offsetX.value > 240f) {
                                                    // Swipe Right -> Like
                                                    offsetX.animateTo(
                                                        1200f,
                                                        animationSpec = tween(250, easing = FastOutSlowInEasing)
                                                    )
                                                    onLike(currentTopUser)
                                                } else if (offsetX.value < -240f) {
                                                    // Swipe Left -> Pass
                                                    offsetX.animateTo(
                                                        -1200f,
                                                        animationSpec = tween(250, easing = FastOutSlowInEasing)
                                                    )
                                                    onPass(currentTopUser)
                                                } else {
                                                    // Snap back to center
                                                    launch { offsetX.animateTo(0f, spring()) }
                                                    launch { offsetY.animateTo(0f, spring()) }
                                                }
                                            }
                                        }
                                    )
                                }
                                .clickable { onCardClick(currentTopUser) }
                                .testTag("top_interactive_card")
                        ) {
                            SwipeCardItem(
                                user = currentTopUser,
                                modifier = Modifier.fillMaxSize()
                            )

                            // LIKE Stamp (green, rotated -15°)
                            if (likeAlpha > 0.05f) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(24.dp)
                                        .rotate(-15f)
                                        .border(3.5.dp, Color(0xFF10B981), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                        .alpha(likeAlpha)
                                        .testTag("like_stamp")
                                ) {
                                    Text(
                                        text = "LIKE",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 28.sp,
                                        color = Color(0xFF10B981)
                                    )
                                }
                            }

                            // NOPE Stamp (red, rotated 15°)
                            if (nopeAlpha > 0.05f) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(24.dp)
                                        .rotate(15f)
                                        .border(3.5.dp, Color(0xFFEF4444), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                        .alpha(nopeAlpha)
                                        .testTag("nope_stamp")
                                ) {
                                    Text(
                                        text = "NOPE",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 28.sp,
                                        color = Color(0xFFEF4444)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action buttons row: Rewind (48dp circle white) + Pass (64dp circle white, red X) + Like (64dp circle gradient, white heart)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rewind button (48dp circle white)
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.dp, Color(0xFF000000), CircleShape)
                            .clickable { onRewind() }
                            .testTag("action_rewind"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay,
                            contentDescription = "Rewind",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Pass button (64dp circle white, red X)
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.5.dp, Color(0xFF000000), CircleShape)
                            .clickable {
                                if (currentTopUser != null) {
                                    coroutineScope.launch {
                                        offsetX.animateTo(-1200f, tween(250))
                                        onPass(currentTopUser)
                                    }
                                }
                            }
                            .testTag("action_pass"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Pass",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Like button (64dp circle gradient, white heart)
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(AuroraHorizontalGradient)
                            .border(2.5.dp, Color(0xFF000000), CircleShape)
                            .clickable {
                                if (currentTopUser != null) {
                                    coroutineScope.launch {
                                        offsetX.animateTo(1200f, tween(250))
                                        onLike(currentTopUser)
                                    }
                                }
                            }
                            .testTag("action_like"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Like",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SwipeCardItem(
    user: User,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier
            .border(2.dp, Color(0xFF000000), RoundedCornerShape(24.dp))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = user.photos.firstOrNull() ?: "",
                contentDescription = user.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Top-right: "Online" / "NEW" badge if applicable
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (user.isOnline) {
                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(Color(0xE610B981))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ONLINE",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = Color.White
                        )
                    }
                }
            }

            // Bottom gradient overlay (#2A1B3D 80%)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0x992A1B3D),
                                Color(0xCC2A1B3D),
                                Color(0xF22A1B3D)
                            )
                        )
                    )
            )

            // Bottom-left: Name + age (Poppins Bold 26sp white) + verified badge + 📍 distance + bio (2 lines) + interest chips
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.nameWithAge,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        color = Color.White
                    )
                    if (user.verified) {
                        Spacer(modifier = Modifier.width(6.dp))
                        VerifiedBadge(size = 22.dp)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFFFF6B9D),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${user.distance} away • ${user.country}",
                        fontFamily = InterFontFamily,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = user.bio,
                    fontFamily = InterFontFamily,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.92f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    user.interests.take(3).forEach { interest ->
                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(Color.White.copy(alpha = 0.22f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = interest,
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
