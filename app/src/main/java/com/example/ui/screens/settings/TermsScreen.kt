package com.example.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SoulTopBar
import com.example.ui.theme.AppBackground
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TermsScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("terms_screen")
    ) {
        SoulTopBar(
            title = "Terms of Service",
            showBack = true,
            onBackClick = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp)
        ) {
            Text(
                text = "Last updated: October 2026",
                fontFamily = InterFontFamily,
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            TermsSection(
                title = "1. Acceptance of Terms",
                body = "By downloading, accessing, or using Soulmate, you agree to be bound by these Terms of Service and all applicable laws and regulations. If you do not agree with any of these terms, you are prohibited from using the platform."
            )

            TermsSection(
                title = "2. Eligibility & Age Restriction",
                body = "You must be at least 18 years of age to create an account on Soulmate. By creating an account, you represent and warrant that you can legally enter into a binding contract and are not prohibited from using the service under applicable laws."
            )

            TermsSection(
                title = "3. Community Safety & Verification",
                body = "Soulmate strictly requires genuine identity representations. You agree to upload only your own authentic photos and honest personal information. Any impersonation, deceptive behavior, or harassment will result in immediate and permanent account suspension."
            )

            TermsSection(
                title = "4. User Content & Conduct",
                body = "You retain ownership of the content and photos you post. However, you grant Soulmate a worldwide license to host, display, and process your content solely for the purpose of operating and improving the matchmaking experience."
            )

            TermsSection(
                title = "5. Termination",
                body = "In-app account deletion is not available in this build. Contact the app developer to request account removal. Soulmate may suspend or terminate accounts that breach safety guidelines or engage in abusive interactions."
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun CommunityRulesScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("community_rules_screen")
    ) {
        SoulTopBar(
            title = "Community Rules",
            showBack = true,
            onBackClick = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp)
        ) {
            Text(
                text = "Soulmate is built on empathy, kindness, and authentic human connection. Please adhere to these guiding principles.",
                fontFamily = InterFontFamily,
                fontSize = 14.sp,
                color = TextSecondary,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            TermsSection(
                title = "Be Kind & Respectful",
                body = "Treat every soulmate with dignity. Harassment, hate speech, body shaming, and discriminatory remarks are never tolerated under any circumstances."
            )

            TermsSection(
                title = "Authenticity Matters",
                body = "Use real, recent photos of yourself. Do not use AI-generated impostors, stock photos, or impersonate other public figures."
            )

            TermsSection(
                title = "Consent & Boundaries",
                body = "Respect when someone chooses to pass or decline a chat. Continuous unsolicited messages after a boundary is set constitutes harassment."
            )

            TermsSection(
                title = "Zero Tolerance for Scams",
                body = "Do not solicit money, cryptocurrency, financial investments, or commercial services on Soulmate. Suspected scams may be reported to the app developer."
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun TermsSection(title: String, body: String) {
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        Text(
            text = title,
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = body,
            fontFamily = InterFontFamily,
            fontSize = 14.sp,
            color = TextSecondary,
            lineHeight = 21.sp
        )
    }
}
