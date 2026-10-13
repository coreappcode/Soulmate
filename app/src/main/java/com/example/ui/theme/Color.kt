package com.example.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Aurora Warm Primary Gradient Colors
val RosePrimary = Color(0xFFFF6B9D)       // warm rose
val OrchidPrimary = Color(0xFFC86DD7)     // orchid purple
val PeriwinklePrimary = Color(0xFF6E8EFB) // periwinkle blue

val AuroraGradientColors = listOf(RosePrimary, OrchidPrimary, PeriwinklePrimary)

val AuroraGradient = Brush.linearGradient(
    colors = AuroraGradientColors,
    start = Offset(0f, 0f),
    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
)

val AuroraHorizontalGradient = Brush.horizontalGradient(AuroraGradientColors)

// Backgrounds
val AppBackground = Color(0xFFFFF7F2)     // warm cream — never pure white/black
val CardBackground = Color(0xFFFFFFFF)    // only cards use pure white
val ElevatedSurface = Color(0xFFFDF2F8)   // soft blush
val DarkAccent = Color(0xFF2A1B3D)        // deep plum — used sparingly, never as main bg
val HeaderBackground = Color(0xFFFBE9F1)  // soft rose blush for top app bar
val BottomNavBackground = Color(0xFFF3E8FF) // soft lavender for bottom nav
val HairlineDivider = Color(0xFFF0E6EC)   // 1dp hairline divider
val ActiveTabLabel = Color(0xFFC86DD7)    // active tab label orchid
val InactiveTabColor = Color(0xFFA99BB5)  // inactive tab lavender-gray

// Text Colors
val TextPrimary = Color(0xFF1A1423)       // near-black plum, never pure black
val TextSecondary = Color(0xFF6B5B7A)     // muted plum-gray
val TextMuted = Color(0xFFA99BB5)         // light lavender-gray
val TextOnGradient = Color(0xFFFFFFFF)

// Semantic
val SemanticSuccess = Color(0xFF34D399)   // mint
val SemanticOnline = Color(0xFF34D399)    // mint
val SemanticWarning = Color(0xFFFBBF24)   // amber
val SemanticError = Color(0xFFF87171)     // soft coral red
val SemanticLike = Color(0xFFFF6B9D)      // matches gradient start
val SemanticVerified = Color(0xFF6E8EFB)  // periwinkle

// Borders
val BorderSubtle = Color(0xFF1A1423)
val BorderBlack = Color(0xFF000000)
val BorderFocus = Color(0xFF000000)

// Brand Tinted Shadows (never gray/black)
val BrandShadowColor = Color(0x1FC86DD7)
val BrandShadowSoft = Color(0x14C86DD7)
val BrandShadowElevated = Color(0x2EC86DD7)
