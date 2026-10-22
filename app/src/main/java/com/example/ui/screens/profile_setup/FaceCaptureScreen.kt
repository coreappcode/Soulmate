package com.example.ui.screens.profile_setup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.GradientButton
import com.example.ui.components.ProgressDots
import com.example.ui.components.SoulOutlinedButton
import com.example.ui.theme.AppBackground
import com.example.ui.theme.AuroraHorizontalGradient
import com.example.ui.theme.H2Style
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.OrchidPrimary
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.RosePrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.utils.createCameraCaptureUri

@Composable
fun FaceCaptureScreen(
    onFaceCaptured: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var isCapturing by remember { mutableStateOf(false) }
    var capturedPhotoUrl by remember { mutableStateOf<String?>(null) }
    var photoError by remember { mutableStateOf<String?>(null) }
    var cameraCaptureUri by remember { mutableStateOf<Uri?>(null) }

    // Real Camera Selfie capture launcher
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        isCapturing = false
        if (success && cameraCaptureUri != null) {
            capturedPhotoUrl = cameraCaptureUri.toString()
        } else {
            photoError = "The camera did not capture a selfie. Please try again."
        }
    }

    // Real Photo Picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        isCapturing = false
        if (uri != null) {
            capturedPhotoUrl = uri.toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("face_capture_screen")
    ) {
        // Top header with back button and title
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
                    .testTag("face_capture_back")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }

            Text(
                text = "Upload a Selfie",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = TextPrimary,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Add a profile selfie",
                style = H2Style,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "You can store a selfie with your profile. This app does not currently verify identity.",
                fontFamily = InterFontFamily,
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Circular Camera Frame (240dp)
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .border(3.dp, AuroraHorizontalGradient, CircleShape)
                    .background(Color(0xFFF7ECF2)),
                contentAlignment = Alignment.Center
            ) {
                if (capturedPhotoUrl != null) {
                    AsyncImage(
                        model = capturedPhotoUrl,
                        contentDescription = "Captured Selfie Preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (isCapturing) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = OrchidPrimary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Opening camera...",
                            fontFamily = InterFontFamily,
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                } else {
                    // Face guide oval overlay
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawOval(
                            color = OrchidPrimary.copy(alpha = 0.4f),
                            topLeft = Offset(size.width * 0.2f, size.height * 0.12f),
                            size = Size(size.width * 0.6f, size.height * 0.76f),
                            style = Stroke(
                                width = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                            )
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = TextMuted.copy(alpha = 0.4f),
                        modifier = Modifier.size(100.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (capturedPhotoUrl != null) {
                    "Selfie selected. It will not mark your profile as verified."
                } else {
                    "Choose a clear profile selfie"
                },
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = if (capturedPhotoUrl != null) OrchidPrimary else TextSecondary
            )
            if (!photoError.isNullOrBlank()) {
                Text(
                    text = photoError.orEmpty(),
                    color = RosePrimary,
                    fontFamily = InterFontFamily,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }

        // Action Buttons at bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            if (capturedPhotoUrl == null) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GradientButton(
                        text = "Take Selfie (Camera)",
                        onClick = {
                            isCapturing = true
                            photoError = null
                            val uri = context.createCameraCaptureUri("selfie")
                            cameraCaptureUri = uri
                            takePictureLauncher.launch(uri)
                        },
                        loading = isCapturing,
                        modifier = Modifier.fillMaxWidth().testTag("take_selfie_button")
                    )

                    SoulOutlinedButton(
                        text = "Choose from Gallery",
                        onClick = {
                            isCapturing = true
                            photoError = null
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth().testTag("choose_selfie_gallery_button")
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SoulOutlinedButton(
                        text = "Retake",
                        onClick = { capturedPhotoUrl = null },
                        modifier = Modifier.weight(1f)
                    )

                    GradientButton(
                        text = "Use This",
                        onClick = {
                            capturedPhotoUrl?.let { onFaceCaptured(it) }
                        },
                        modifier = Modifier.weight(1.3f)
                    )
                }
            }
        }
    }
}
