package com.example.ui.screens.profile_setup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.GradientButton
import com.example.ui.components.ProgressDots
import com.example.ui.theme.AppBackground
import com.example.ui.theme.AuroraHorizontalGradient
import com.example.ui.theme.BorderFocus
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DarkAccent
import com.example.ui.theme.H2Style
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.OrchidPrimary
import com.example.ui.theme.PillShape
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.PhotoLibrary
import com.example.ui.components.SoulOutlinedButton

@Composable
fun PhotosSetupScreen(
    photos: List<String>,
    onAddPhotoClick: () -> Unit,
    onPhotoSelected: (String) -> Unit = {},
    onRemovePhoto: (Int) -> Unit,
    onSetMainPhoto: (Int) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    val canContinue = photos.size >= 2

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onPhotoSelected(uri.toString())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("photos_setup_screen")
    ) {
        // Top row with back button and ProgressDots
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.CenterStart)
                    .testTag("photos_back")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }

            ProgressDots(
                total = 5,
                current = 2,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Add your best photos",
                style = H2Style
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Add at least 2 to continue. Tap any photo to set it as main.",
                fontFamily = InterFontFamily,
                fontSize = 14.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(28.dp))

            // 6-slot grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(6) { index ->
                    val photoUrl = photos.getOrNull(index)
                    val isMain = index == 0 && photoUrl != null
                    val slotShape = RoundedCornerShape(16.dp)

                    if (photoUrl != null) {
                        // Filled photo slot
                        Box(
                            modifier = Modifier
                                .aspectRatio(0.75f)
                                .clip(slotShape)
                                .clickable { onSetMainPhoto(index) }
                                .testTag("photo_slot_$index")
                        ) {
                            AsyncImage(
                                model = photoUrl,
                                contentDescription = "User Photo $index",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Main badge for slot 1
                            if (isMain) {
                                Box(
                                    modifier = Modifier
                                        .padding(6.dp)
                                        .clip(PillShape)
                                        .background(AuroraHorizontalGradient)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                        .align(Alignment.TopStart)
                                ) {
                                    Text(
                                        text = "Main",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = Color.White
                                    )
                                }
                            }

                            // Remove button top-right
                            Box(
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(DarkAccent.copy(alpha = 0.65f))
                                    .clickable { onRemovePhoto(index) }
                                    .align(Alignment.TopEnd),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove Photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    } else {
                        // Empty slot with dashed border
                        Box(
                            modifier = Modifier
                                .aspectRatio(0.75f)
                                .clip(slotShape)
                                .background(Color.White)
                                .drawBehind {
                                    val stroke = Stroke(
                                        width = 2.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(
                                            intervals = floatArrayOf(16f, 16f),
                                            phase = 0f
                                        )
                                    )
                                    drawRoundRect(
                                        color = BorderFocus.copy(alpha = 0.5f),
                                        size = size,
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx()),
                                        style = stroke
                                    )
                                }
                                .clickable { onAddPhotoClick() }
                                .testTag("empty_photo_slot_$index"),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(BorderSubtle),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Photo",
                                    tint = OrchidPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SoulOutlinedButton(
                text = "Choose from Gallery",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        tint = OrchidPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("photos_setup_gallery_button")
            )
        }

        // Bottom sticky button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            GradientButton(
                text = "Continue (${photos.size}/6)",
                onClick = onContinue,
                enabled = canContinue,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
