package com.example.ui.screens.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.GradientButton
import com.example.ui.components.SoulOutlinedButton
import com.example.ui.components.SoulTopBar
import com.example.ui.theme.AppBackground
import com.example.ui.theme.CardBackground
import com.example.ui.theme.HairlineDivider
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.OrchidPrimary
import com.example.ui.theme.PillShape
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.RosePrimary
import com.example.ui.theme.SemanticOnline
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun EditPhotosScreen(
    photos: List<String>,
    onAddPhoto: (String) -> Unit,
    onRemovePhoto: (Int) -> Unit,
    onSetMainPhoto: (Int) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var pendingCropModel by remember { mutableStateOf<String?>(null) }

    // Real Android Photo Picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            pendingCropModel = uri.toString()
        }
    }

    if (pendingCropModel != null) {
        // Android photo crop / adjustment view
        PhotoCropAdjustmentView(
            imageModel = pendingCropModel!!,
            onCancel = { pendingCropModel = null },
            onConfirm = { croppedUriString ->
                if (!photos.contains(croppedUriString)) {
                    onAddPhoto(croppedUriString)
                }
                pendingCropModel = null
                Toast.makeText(context, "Photo updated in your profile!", Toast.LENGTH_SHORT).show()
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("edit_photos_screen")
    ) {
        SoulTopBar(
            title = "Edit Photos",
            showBack = true,
            onBackClick = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Profile Photos (Max 6)",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap any photo to make it your Main profile photo. Slot 1 is shown first to people nearby.",
                fontFamily = InterFontFamily,
                fontSize = 13.sp,
                color = TextSecondary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(6) { index ->
                    val photoUrl = photos.getOrNull(index)
                    val isMain = index == 0 && photoUrl != null

                    if (photoUrl != null) {
                        // Filled photo slot
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(3f / 4f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(CardBackground)
                                .border(
                                    width = if (isMain) 2.5.dp else 1.5.dp,
                                    color = if (isMain) OrchidPrimary else Color(0xFF000000),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    if (!isMain) {
                                        onSetMainPhoto(index)
                                        Toast.makeText(context, "Set as main photo", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .testTag("photo_slot_$index")
                        ) {
                            AsyncImage(
                                model = photoUrl,
                                contentDescription = "Photo $index",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Main Badge
                            if (isMain) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(6.dp)
                                        .clip(PillShape)
                                        .background(OrchidPrimary)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.size(2.dp))
                                        Text(
                                            text = "MAIN",
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            // Remove X Button
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xCC000000))
                                    .clickable { onRemovePhoto(index) }
                                    .testTag("remove_photo_$index"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove Photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            // Crop Photo Button
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(6.dp)
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xCC1A1423))
                                    .clickable { pendingCropModel = photoUrl }
                                    .testTag("crop_photo_$index"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Crop,
                                    contentDescription = "Crop Photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    } else {
                        // Dashed empty slot
                        val stroke = Stroke(
                            width = 3f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(3f / 4f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF9F0F4))
                                .drawBehind {
                                    drawRoundRect(
                                        color = Color(0xFFC4B5C1),
                                        style = stroke,
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx())
                                    )
                                }
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                .testTag("empty_photo_slot_$index"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .border(1.dp, Color(0xFF000000), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Photo",
                                        tint = TextPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Add",
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action button to launch gallery
            SoulOutlinedButton(
                text = "Choose from Gallery",
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Sticky save bottom bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppBackground)
                .border(width = 1.dp, color = HairlineDivider)
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            GradientButton(
                text = "Done Editing",
                onClick = onSave,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PhotoCropAdjustmentView(
    imageModel: String,
    onCancel: () -> Unit,
    onConfirm: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("photo_crop_view")
    ) {
        SoulTopBar(
            title = "Crop & Adjust",
            showBack = true,
            onBackClick = onCancel
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Adjust photo framing for your 3:4 profile card",
                fontFamily = InterFontFamily,
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3:4 aspect ratio crop frame
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(3f / 4f)
                    .clip(RoundedCornerShape(20.dp))
                    .border(2.5.dp, OrchidPrimary, RoundedCornerShape(20.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = "Crop Preview",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Crop grid lines
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(1.dp, Color.White.copy(alpha = 0.35f))
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Crop,
                    contentDescription = null,
                    tint = OrchidPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "3:4 Aspect Ratio Locked",
                    fontFamily = PoppinsFontFamily,
                    fontSize = 12.sp,
                    color = TextPrimary
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppBackground)
                .border(width = 1.dp, color = HairlineDivider)
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SoulOutlinedButton(
                    text = "Cancel",
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                )

                GradientButton(
                    text = "Confirm Photo",
                    onClick = { onConfirm(imageModel) },
                    modifier = Modifier.weight(1.3f)
                )
            }
        }
    }
}
