package com.example.ui.screens.settings

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GradientButton
import com.example.ui.components.SoulOutlinedButton
import com.example.ui.components.SoulTopBar
import com.example.ui.theme.AppBackground
import com.example.ui.theme.AuroraHorizontalGradient
import com.example.ui.theme.CardBackground
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.OrchidPrimary
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.SemanticOnline
import com.example.ui.theme.SemanticWarning
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun VerificationStatusScreen(
    isVerified: Boolean,
    onVerifyNow: () -> Unit,
    onReverify: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("verification_status_screen")
    ) {
        SoulTopBar(
            title = "Verification Status",
            showBack = true,
            onBackClick = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Gradient status card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .then(
                        if (isVerified) {
                            Modifier
                                .background(AuroraHorizontalGradient)
                                .border(2.dp, Color(0xFF000000), RoundedCornerShape(24.dp))
                        } else {
                            Modifier
                                .background(Color(0xFFFEF3C7))
                                .border(2.dp, SemanticWarning, RoundedCornerShape(24.dp))
                        }
                    )
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.5.dp, Color(0xFF000000), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isVerified) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isVerified) OrchidPrimary else Color(0xFFD97706),
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (isVerified) "Profile Verified" else "Verification Pending",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = if (isVerified) Color.White else Color(0xFF92400E)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isVerified) "Verified on Oct 12, 2026 • Valid for 1 year" else "Verify your face to unlock unlimited messaging & trust",
                        fontFamily = InterFontFamily,
                        fontSize = 13.sp,
                        color = if (isVerified) Color.White.copy(alpha = 0.9f) else Color(0xFFB45309)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // "What verification unlocks" list
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color(0xFF000000), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "What Verification Unlocks",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    listOf(
                        "Blue Verified Badge on your profile & discovery card" to true,
                        "3x more likes and mutual match recommendations" to true,
                        "Direct instant messaging with all soulmates" to true,
                        "Higher safety score and scam-shield protection" to true
                    ).forEach { (benefit, isUnlocked) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFD1FAE5)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = SemanticOnline,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = benefit,
                                fontFamily = InterFontFamily,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (isVerified) {
                SoulOutlinedButton(
                    text = "Re-verify Face Identity",
                    onClick = onReverify,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                GradientButton(
                    text = "Verify Now",
                    onClick = onVerifyNow,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
