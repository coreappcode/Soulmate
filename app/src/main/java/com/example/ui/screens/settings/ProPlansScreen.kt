package com.example.ui.screens.settings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.GooglePlayBillingManager
import com.example.data.GooglePlayPlan
import com.example.ui.components.GradientButton
import com.example.ui.components.SoulTextButton
import com.example.ui.components.SoulTopBar
import com.example.ui.theme.AppBackground
import com.example.ui.theme.AuroraHorizontalGradient
import com.example.ui.theme.CardBackground
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.OrchidPrimary
import com.example.ui.theme.SemanticOnline
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.utils.findActivity

@Composable
fun ProPlansScreen(
    isPro: Boolean,
    onVerifyPurchase: suspend (purchaseToken: String, productId: String) -> Unit,
    onAccessUpdated: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val billingManager = remember(context, scope) {
        GooglePlayBillingManager(
            context = context,
            scope = scope,
            verifyPurchase = onVerifyPurchase,
            onAccessUpdated = onAccessUpdated
        )
    }
    val plans by billingManager.plans.collectAsState()
    val isReady by billingManager.isReady.collectAsState()
    val isProcessing by billingManager.isProcessing.collectAsState()
    val billingMessage by billingManager.message.collectAsState()
    var selectedProductId by remember { mutableStateOf("soulmate_pro_monthly") }
    val selectedPlan = plans.firstOrNull { it.productId == selectedProductId }
        ?: plans.firstOrNull()

    LaunchedEffect(plans) {
        if (plans.none { it.productId == selectedProductId }) {
            plans.firstOrNull()?.let { selectedProductId = it.productId }
        }
    }

    DisposableEffect(billingManager) {
        billingManager.connect()
        onDispose { billingManager.disconnect() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("pro_plans_screen")
    ) {
        SoulTopBar(
            title = "Soulmate Pro",
            showBack = true,
            onBackClick = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(AuroraHorizontalGradient)
                    .border(2.dp, Color.Black, RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = OrchidPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Soulmate Pro",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isPro) {
                            "Your Pro subscription is active."
                        } else {
                            "Unlimited swipes. Rewind any swipe."
                        },
                        fontFamily = InterFontFamily,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color.Black, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    listOf("Unlimited daily swipes", "Unlimited rewinds").forEach { feature ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFD1FAE5)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = SemanticOnline,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = feature,
                                fontFamily = InterFontFamily,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (plans.isEmpty()) {
                if (billingMessage == null) {
                    CircularProgressIndicator(color = OrchidPrimary)
                }
                Text(
                    text = billingMessage
                        ?: if (isReady) "Loading plans from Google Play…" else "Connecting to Google Play…",
                    fontFamily = InterFontFamily,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    plans.forEachIndexed { index, plan ->
                        PlanCard(
                            plan = plan,
                            selected = selectedPlan?.productId == plan.productId,
                            index = index,
                            onClick = { selectedProductId = plan.productId }
                        )
                    }
                }
            }

            if (!billingMessage.isNullOrBlank()) {
                Text(
                    text = billingMessage.orEmpty(),
                    fontFamily = InterFontFamily,
                    fontSize = 13.sp,
                    color = if (isPro) SemanticOnline else TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )
            }

            SoulTextButton(
                text = "Restore purchases",
                onClick = billingManager::restorePurchases,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = "Subscriptions renew automatically unless canceled in Google Play. Prices and currency are provided by Google Play for your country.",
                fontFamily = InterFontFamily,
                fontSize = 11.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 10.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppBackground)
                .border(width = 1.dp, color = Color.Black.copy(alpha = 0.08f))
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            val activity = context.findActivity()
            GradientButton(
                text = when {
                    isPro -> "Soulmate Pro active"
                    isProcessing -> "Verifying purchase…"
                    selectedPlan != null -> "Subscribe — ${selectedPlan.formattedPrice}"
                    else -> "Plans unavailable"
                },
                onClick = {
                    if (activity != null && selectedPlan != null && !isPro && !isProcessing) {
                        billingManager.purchase(activity, selectedPlan.productId)
                    }
                },
                enabled = activity != null && selectedPlan != null && isReady && !isPro && !isProcessing,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PlanCard(
    plan: GooglePlayPlan,
    selected: Boolean,
    index: Int,
    onClick: () -> Unit
) {
    val cadence = when (plan.productId) {
        "soulmate_pro_monthly" -> "Billed monthly"
        "soulmate_pro_quarterly" -> "Billed every 3 months"
        "soulmate_pro_yearly" -> "Billed yearly"
        else -> "Subscription"
    }
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) ElevatedSurface else Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (selected) 2.5.dp else 1.5.dp,
                color = if (selected) OrchidPrimary else Color.Black,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .testTag("pricing_plan_$index")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = plan.displayName,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = cadence,
                    fontFamily = InterFontFamily,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            Text(
                text = plan.formattedPrice,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = if (selected) OrchidPrimary else TextPrimary
            )
        }
    }
}
