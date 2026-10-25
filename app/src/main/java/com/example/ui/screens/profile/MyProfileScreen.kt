package com.example.ui.screens.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.UserProfileState
import com.example.ui.components.ProgressDots
import com.example.ui.components.SoulCard
import com.example.ui.components.SoulChip
import com.example.ui.components.SoulOutlinedButton
import com.example.ui.components.SoulTopBar
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.AppBackground
import com.example.ui.theme.AuroraHorizontalGradient
import com.example.ui.theme.CardBackground
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.OrchidPrimary
import com.example.ui.theme.PillShape
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.RosePrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MyProfileScreen(
    userProfile: UserProfileState,
    onNavigateToSettings: () -> Unit,
    onNavigateToEditPhotos: () -> Unit,
    onNavigateToEditBasicInfo: () -> Unit,
    onNavigateToEditAbout: () -> Unit,
    onNavigateToEditDetails: () -> Unit,
    onNavigateToPreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    val photos = userProfile.photos
    val pagerState = rememberPagerState(pageCount = { photos.size })

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("my_profile_screen")
    ) {
        // Top bar (#FBE9F1): "Profile" + settings icon (right)
        SoulTopBar(
            title = "Profile",
            showBack = false,
            actions = {
                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("profile_top_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White
                    )
                }
            }
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // Photo carousel (HorizontalPager, 3:4 aspect) + dots + floating "Edit Photos" button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(3f / 4f)
                        .testTag("profile_photo_carousel")
                ) {
                    if (photos.isNotEmpty()) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            AsyncImage(
                                model = photos[page],
                                contentDescription = "Profile Photo $page",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(ElevatedSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Add photos to complete your profile",
                                fontFamily = InterFontFamily,
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    // Dots indicator
                    if (photos.size > 1) {
                        ProgressDots(
                            total = photos.size,
                            current = pagerState.currentPage,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .offset(y = (-44).dp)
                        )
                    }

                    // Floating "Edit Photos" button at bottom-right of carousel
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = (-16).dp, y = (-40).dp)
                            .clip(PillShape)
                            .background(Color.White)
                            .border(1.5.dp, Color(0xFF000000), PillShape)
                            .clickable { onNavigateToEditPhotos() }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("edit_photos_floating_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = TextPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Edit Photos",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = TextPrimary
                            )
                        }
                    }
                }

                // Below: cream section rounded top 32dp overlapping photo
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-24).dp)
                        .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                        .background(AppBackground)
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Header with Name + Age + VerifiedBadge & Edit Basic icon
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val displayName = userProfile.fullName.ifBlank { "Complete your profile" }
                                Text(
                                    text = if (userProfile.age > 0) "$displayName, ${userProfile.age}" else displayName,
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 26.sp,
                                    color = TextPrimary
                                )
                                if (userProfile.isFaceVerified) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    VerifiedBadge(size = 20.dp)
                                }
                            }

                            IconButton(
                                onClick = onNavigateToEditBasicInfo,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFF000000), CircleShape)
                                    .testTag("edit_basic_info_icon")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Basic Info",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // @username (tappable -> copied snackbar)
                        val handle = userProfile.username.takeIf(String::isNotBlank)?.let { "@$it" }
                        Text(
                            text = handle ?: "Username not set",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = OrchidPrimary,
                            modifier = Modifier
                                .then(
                                    if (handle == null) Modifier
                                    else Modifier.clickable {
                                        clipboardManager.setText(AnnotatedString(handle))
                                        Toast.makeText(
                                            context,
                                            "Copied $handle to clipboard",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                                .padding(vertical = 4.dp)
                                .testTag("username_copy_handle")
                        )

                        // Location: City, Country
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = RosePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${userProfile.city}, ${userProfile.selectedCountry.name}",
                                fontFamily = InterFontFamily,
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }

                        // Profile strength indicator
                        val strength = userProfile.profileStrength
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .border(1.5.dp, Color(0xFF000000), RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Profile Strength",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${(strength * 100).toInt()}%",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = OrchidPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { strength },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(PillShape),
                                    color = OrchidPrimary,
                                    trackColor = ElevatedSurface
                                )
                                if (strength < 1f) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Add more photos and fill details to get 3x more matches!",
                                        fontFamily = InterFontFamily,
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Bio with Edit icon
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.5.dp, Color(0xFF000000), RoundedCornerShape(20.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Bio",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = TextPrimary
                                    )
                                    IconButton(
                                        onClick = onNavigateToEditAbout,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Bio",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = userProfile.bio.ifBlank { "No bio added yet. Click edit to tell your soulmates about yourself!" },
                                    fontFamily = InterFontFamily,
                                    fontSize = 14.sp,
                                    color = TextSecondary,
                                    lineHeight = 21.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Interests FlowRow chips
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.5.dp, Color(0xFF000000), RoundedCornerShape(20.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Interests",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = TextPrimary
                                    )
                                    IconButton(
                                        onClick = onNavigateToEditAbout,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Interests",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    userProfile.selectedInterests.forEach { interest ->
                                        Box(
                                            modifier = Modifier
                                                .clip(PillShape)
                                                .background(ElevatedSurface)
                                                .border(1.dp, Color(0xFF000000), PillShape)
                                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = interest,
                                                fontFamily = InterFontFamily,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 13.sp,
                                                color = TextPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // "Looking For" section
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.5.dp, Color(0xFF000000), RoundedCornerShape(20.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Looking For",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    val lookingForList = if (userProfile.selectedLookingFor.isNotEmpty()) userProfile.selectedLookingFor else listOf("Long-term partner")
                                    lookingForList.forEach { item ->
                                        Box(
                                            modifier = Modifier
                                                .clip(PillShape)
                                                .background(AuroraHorizontalGradient)
                                                .border(1.dp, Color(0xFF000000), PillShape)
                                                .padding(horizontal = 16.dp, vertical = 7.dp)
                                        ) {
                                            Text(
                                                text = item,
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // "Basics" 2-col grid (Height, Job, Education, Zodiac)
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.5.dp, Color(0xFF000000), RoundedCornerShape(20.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Basics",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = TextPrimary
                                    )
                                    IconButton(
                                        onClick = onNavigateToEditDetails,
                                        modifier = Modifier
                                            .size(28.dp)
                                            .testTag("edit_profile_details_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit profile details",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(modifier = Modifier.fillMaxWidth()) {
                                    BasicsItem(
                                        icon = Icons.Default.Straighten,
                                        label = "Height",
                                        value = userProfile.height,
                                        modifier = Modifier.weight(1f)
                                    )
                                    BasicsItem(
                                        icon = Icons.Default.Work,
                                        label = "Work",
                                        value = userProfile.job,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(modifier = Modifier.fillMaxWidth()) {
                                    BasicsItem(
                                        icon = Icons.Default.School,
                                        label = "Education",
                                        value = userProfile.education,
                                        modifier = Modifier.weight(1f)
                                    )
                                    BasicsItem(
                                        icon = Icons.Default.Star,
                                        label = "Zodiac",
                                        value = userProfile.zodiac,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }

            // Sticky bottom: "Preview as others see me" SoulOutlinedButton
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(AppBackground)
                    .border(
                        width = 1.dp,
                        color = Color(0xFF000000).copy(alpha = 0.08f)
                    )
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                SoulOutlinedButton(
                    text = "Preview as others see me",
                    onClick = onNavigateToPreview,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun BasicsItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(ElevatedSurface)
                .border(1.dp, Color(0xFF000000), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = OrchidPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = label,
                fontFamily = InterFontFamily,
                fontSize = 11.sp,
                color = TextSecondary
            )
            Text(
                text = value,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = TextPrimary
            )
        }
    }
}
