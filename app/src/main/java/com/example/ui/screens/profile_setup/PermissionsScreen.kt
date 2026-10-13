package com.example.ui.screens.profile_setup

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GradientButton
import com.example.ui.components.SoulCard
import com.example.ui.components.SoulTextButton
import com.example.ui.theme.AppBackground
import com.example.ui.theme.AuroraHorizontalGradient
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.H2Style
import com.example.ui.theme.H3Style
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.OrchidPrimary
import com.example.ui.theme.PillShape
import com.example.ui.theme.RosePrimary
import com.example.ui.theme.SemanticOnline
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

private data class ConfettiParticle(
    val x: Float,
    val speedY: Float,
    val size: Float,
    val color: Color
)

@Composable
fun PermissionsScreen(
    locationAllowed: Boolean,
    notificationsAllowed: Boolean,
    onAllowLocation: () -> Unit,
    onAllowNotifications: () -> Unit,
    onEnterSoulmate: () -> Unit,
    onSkip: () -> Unit
) {
    var isCelebrating by remember { mutableStateOf(false) }
    val confettiProgress = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    val particles = remember {
        val colors = listOf(Color(0xFFFF6B9D), Color(0xFFC86DD7), Color(0xFF6E8EFB), Color(0xFF34D399), Color(0xFFFBBF24))
        List(60) {
            ConfettiParticle(
                x = Random.nextFloat(),
                speedY = Random.nextFloat() * 0.8f + 0.4f,
                size = Random.nextFloat() * 14f + 8f,
                color = colors[Random.nextInt(colors.size)]
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("permissions_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = "A couple more things",
                style = H2Style
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Soulmate works best with these enabled to help discover connections nearby.",
                fontFamily = InterFontFamily,
                fontSize = 14.sp,
                color = TextSecondary,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Card 1: Location
            PermissionCard(
                icon = Icons.Default.LocationOn,
                title = "Location",
                description = "Required to find people nearby",
                isAllowed = locationAllowed,
                onAllow = onAllowLocation,
                tag = "allow_location_card"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Card 2: Notifications
            PermissionCard(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                description = "Get notified when you match and receive messages",
                isAllowed = notificationsAllowed,
                onAllow = onAllowNotifications,
                tag = "allow_notifications_card"
            )

            Spacer(modifier = Modifier.weight(1f))

            GradientButton(
                text = "Enter Soulmate",
                onClick = {
                    isCelebrating = true
                    coroutineScope.launch {
                        confettiProgress.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(1000, easing = LinearEasing)
                        )
                        delay(200)
                        onEnterSoulmate()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            SoulTextButton(
                text = "Skip for now",
                onClick = onSkip,
                color = TextSecondary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Confetti celebration animation overlay
        if (isCelebrating) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val progress = confettiProgress.value
                particles.forEach { particle ->
                    val curY = size.height * (progress * particle.speedY)
                    val curX = size.width * particle.x
                    drawRect(
                        color = particle.color,
                        topLeft = Offset(curX, curY),
                        size = Size(particle.size, particle.size * 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionCard(
    icon: ImageVector,
    title: String,
    description: String,
    isAllowed: Boolean,
    onAllow: () -> Unit,
    tag: String
) {
    SoulCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(ElevatedSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = OrchidPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = H3Style,
                    fontSize = 17.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontFamily = InterFontFamily,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            if (isAllowed) {
                Row(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(SemanticOnline.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Allowed",
                        tint = SemanticOnline,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Allowed",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = SemanticOnline
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(AuroraHorizontalGradient)
                        .padding(horizontal = 16.dp, vertical = 9.dp)
                        .testTag("allow_button_${title.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Allow",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color.White,
                        modifier = Modifier.clickable { onAllow() }
                    )
                }
            }
        }
    }
}
