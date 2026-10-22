package com.example.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.User
import com.example.data.UserProfileState
import com.example.ui.screens.explore.FullProfileScreen
import com.example.ui.theme.HeaderBackground
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.OrchidPrimary

@Composable
fun PreviewProfileScreen(
    userProfile: UserProfileState,
    onBack: () -> Unit
) {
    val previewUser = User(
        id = "preview_self",
        name = userProfile.fullName,
        username = userProfile.username,
        age = userProfile.age,
        gender = userProfile.gender,
        bio = userProfile.bio,
        photos = userProfile.photos,
        distance = "",
        country = userProfile.selectedCountry.name,
        city = userProfile.city,
        verified = userProfile.isFaceVerified,
        interests = userProfile.selectedInterests,
        lookingFor = userProfile.selectedLookingFor,
        height = userProfile.height,
        job = userProfile.job,
        education = userProfile.education,
        zodiac = userProfile.zodiac
    )

    FullProfileScreen(
        user = previewUser,
        onBack = onBack,
        bannerContent = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HeaderBackground)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .testTag("preview_profile_banner")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = OrchidPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "This is how others see your profile",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = OrchidPrimary
                    )
                }
            }
        }
    )
}
