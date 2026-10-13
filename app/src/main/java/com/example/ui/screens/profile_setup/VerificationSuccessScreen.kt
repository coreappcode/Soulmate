package com.example.ui.screens.profile_setup

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GradientButton
import com.example.ui.theme.AppBackground
import com.example.ui.theme.AuroraGradient
import com.example.ui.theme.BodyStyle
import com.example.ui.theme.H1Style
import com.example.ui.theme.TextSecondary

@Composable
fun VerificationSuccessScreen(
    onContinue: () -> Unit
) {
    var startAnim by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        startAnim = true
    }

    val checkProgress by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "check_anim"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(horizontal = 28.dp)
            .navigationBarsPadding()
            .testTag("verification_success_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Large Animated Checkmark Canvas (120dp)
        Canvas(modifier = Modifier.size(120.dp)) {
            // Gradient circle background
            drawCircle(brush = AuroraGradient)

            // Animated Check path
            if (checkProgress > 0f) {
                val startX = size.width * 0.28f
                val startY = size.height * 0.52f
                val midX = size.width * 0.45f
                val midY = size.height * 0.68f
                val endX = size.width * 0.72f
                val endY = size.height * 0.36f

                val path = Path()
                if (checkProgress <= 0.4f) {
                    val p = checkProgress / 0.4f
                    path.moveTo(startX, startY)
                    path.lineTo(startX + (midX - startX) * p, startY + (midY - startY) * p)
                } else {
                    val p = (checkProgress - 0.4f) / 0.6f
                    path.moveTo(startX, startY)
                    path.lineTo(midX, midY)
                    path.lineTo(midX + (endX - midX) * p, midY + (endY - midY) * p)
                }

                drawPath(
                    path = path,
                    color = Color.White,
                    style = Stroke(
                        width = 8.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "You're verified!",
            style = H1Style,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Your profile now has a blue badge. You can chat with anyone you match with.",
            style = BodyStyle.copy(color = TextSecondary),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(52.dp))

        GradientButton(
            text = "Continue",
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
