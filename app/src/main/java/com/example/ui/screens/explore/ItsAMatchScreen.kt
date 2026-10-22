package com.example.ui.screens.explore

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.User
import com.example.ui.components.GradientButton
import com.example.ui.components.SoulOutlinedButton
import com.example.ui.theme.AuroraGradient
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.PoppinsFontFamily
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun ItsAMatchScreen(
    matchedUser: User,
    myPhotoUrl: String,
    onSendMessage: () -> Unit,
    onKeepSwiping: () -> Unit
) {
    // Sliding animations for avatars
    val leftAvatarOffset = remember { Animatable(-300f) }
    val rightAvatarOffset = remember { Animatable(300f) }
    val textAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            leftAvatarOffset.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
            )
        }
        launch {
            rightAvatarOffset.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
            )
        }
        launch {
            textAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 800, delayMillis = 300)
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AuroraGradient)
            .testTag("its_a_match_screen")
    ) {
        // Confetti Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val random = Random(42)
            val colors = listOf(
                Color.White,
                Color(0xFFFFD166),
                Color(0xFF06D6A0),
                Color(0xFF118AB2),
                Color(0xFFEF476F)
            )

            for (i in 0..70) {
                val x = random.nextFloat() * size.width
                val y = random.nextFloat() * size.height
                val confettiWidth = 10f + random.nextFloat() * 14f
                val confettiHeight = 6f + random.nextFloat() * 8f
                val color = colors[random.nextInt(colors.size)]
                drawRect(
                    color = color.copy(alpha = 0.85f),
                    topLeft = Offset(x, y),
                    size = Size(confettiWidth, confettiHeight)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "It's a Match!",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 38.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "You and ${matchedUser.name} liked each other.",
                fontFamily = InterFontFamily,
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.95f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Two avatars slide in and overlap slightly
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentAlignment = Alignment.Center
            ) {
                // User 1 avatar (My photo)
                AsyncImage(
                    model = myPhotoUrl,
                    contentDescription = "My Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .offset { IntOffset(leftAvatarOffset.value.roundToInt() - 48, 0) }
                        .size(130.dp)
                        .clip(CircleShape)
                        .border(3.5.dp, Color.White, CircleShape)
                )

                // User 2 avatar (Matched User)
                AsyncImage(
                    model = matchedUser.photos.firstOrNull() ?: "",
                    contentDescription = matchedUser.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .offset { IntOffset(rightAvatarOffset.value.roundToInt() + 48, 0) }
                        .size(130.dp)
                        .clip(CircleShape)
                        .border(3.5.dp, Color.White, CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(50.dp))

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(0.85f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.dp, Color(0xFF000000), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    GradientButton(
                        text = "Send Message",
                        onClick = onSendMessage,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                SoulOutlinedButton(
                    text = "Keep Swiping",
                    onClick = onKeepSwiping,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
