package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.AppBackground
import com.example.ui.theme.AuroraHorizontalGradient
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BrandShadowColor
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardShape
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.HairlineDivider
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.OrchidPrimary
import com.example.ui.theme.PillShape
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.RosePrimary
import com.example.ui.theme.SemanticOnline
import com.example.ui.theme.SemanticVerified
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextOnGradient
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SoulCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = CardShape,
                ambientColor = BrandShadowColor,
                spotColor = BrandShadowColor
            )
            .clip(CardShape)
            .background(CardBackground)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
    ) {
        content()
    }
}

@Composable
fun SoulChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    removable: Boolean = false,
    onRemove: (() -> Unit)? = null
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) OrchidPrimary else AppBackground,
        label = "chip_bg"
    )

    Box(
        modifier = modifier
            .clip(PillShape)
            .then(
                if (selected) {
                    Modifier
                        .background(AuroraHorizontalGradient)
                        .border(1.5.dp, AuroraHorizontalGradient, PillShape)
                } else {
                    Modifier
                        .background(AppBackground)
                        .border(1.dp, HairlineDivider, PillShape)
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp)
            .testTag("chip_${text.lowercase().replace(" ", "_")}"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                fontFamily = PoppinsFontFamily,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                fontSize = 13.sp,
                color = if (selected) TextOnGradient else TextPrimary
            )
            if (removable && onRemove != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f))
                        .clickable(onClick = onRemove),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = if (selected) Color.White else TextSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun VerifiedBadge(
    size: Dp = 16.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(SemanticVerified),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Verified Badge",
            tint = Color.White,
            modifier = Modifier.size(size * 0.7f)
        )
    }
}

@Composable
fun OnlineDot(
    size: Dp = 12.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(SemanticOnline)
            .border(2.dp, Color.White, CircleShape)
    )
}

@Composable
fun SoulAvatar(
    imageUrl: String,
    size: Dp = 48.dp,
    showOnlineDot: Boolean = false,
    isVerified: Boolean = false,
    contentDescription: String = "User Avatar",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(size)
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .border(2.dp, HairlineDivider, CircleShape)
        )

        if (showOnlineDot) {
            OnlineDot(
                size = size * 0.28f,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }

        if (isVerified) {
            VerifiedBadge(
                size = size * 0.32f,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    }
}

@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(PillShape)
            .background(ElevatedSurface)
            .padding(4.dp)
    ) {
        val segmentWidth = maxWidth / options.size
        val indicatorOffset by animateDpAsState(
            targetValue = segmentWidth * selectedIndex,
            animationSpec = tween(300, easing = FastOutSlowInEasing),
            label = "segmented_indicator"
        )

        // Sliding indicator
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(segmentWidth)
                .fillMaxHeight()
                .clip(PillShape)
                .background(AuroraHorizontalGradient)
        )

        // Text labels
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            options.forEachIndexed { index, option ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(PillShape)
                        .clickable { onSelect(index) }
                        .testTag("segment_${option.lowercase().replace(" ", "_")}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = if (selectedIndex == index) FontWeight.SemiBold else FontWeight.Medium,
                        fontSize = 14.sp,
                        color = if (selectedIndex == index) TextOnGradient else TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun ProgressDots(
    total: Int,
    current: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until total) {
            val isCurrent = i == current
            val width by animateDpAsState(
                targetValue = if (isCurrent) 24.dp else 8.dp,
                label = "progress_dot_width"
            )

            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(PillShape)
                    .then(
                        if (isCurrent) {
                            Modifier.background(AuroraHorizontalGradient)
                        } else if (i < current) {
                            Modifier.background(OrchidPrimary.copy(alpha = 0.5f))
                        } else {
                            Modifier.background(BorderSubtle)
                        }
                    )
            )

            if (i < total - 1) {
                Spacer(modifier = Modifier.width(6.dp))
            }
        }
    }
}
