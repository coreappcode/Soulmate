package com.example.ui.screens.profile_setup

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.example.ui.cropper.InsetsAwareCropImageContract
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SoulTopBar
import com.example.ui.components.GradientButton
import com.example.ui.components.SoulTextButton
import com.example.ui.theme.AppBackground
import com.example.ui.theme.AuroraHorizontalGradient
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CardBackground
import com.example.ui.theme.H3Style
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.utils.createCameraCaptureUri
import com.example.utils.launchCameraCapture

@Composable
fun AddPhotoScreen(
    onPhotoAdded: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var photoError by remember { mutableStateOf<String?>(null) }
    var cameraCaptureUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var croppedPhotoUri by rememberSaveable { mutableStateOf<String?>(null) }

    val cropImageLauncher = rememberLauncherForActivityResult(
        contract = InsetsAwareCropImageContract()
    ) { result ->
        if (result.isSuccessful) {
            val croppedUri = result.uriContent
            if (croppedUri != null) {
                photoError = null
                croppedPhotoUri = croppedUri.toString()
            } else {
                photoError = "The cropper did not return an image. Please try again."
            }
        } else if (result.error != null) {
            photoError = result.error?.localizedMessage ?: "Unable to crop this photo."
        }
    }

    fun cropPhoto(uri: Uri) {
        try {
            cropImageLauncher.launch(
                CropImageContractOptions(
                    uri = uri,
                    cropImageOptions = CropImageOptions(
                        fixAspectRatio = true,
                        aspectRatioX = 3,
                        aspectRatioY = 4,
                        outputCompressFormat = android.graphics.Bitmap.CompressFormat.JPEG,
                        backgroundColor = android.graphics.Color.BLACK,
                        toolbarColor = android.graphics.Color.BLACK,
                        toolbarTitleColor = android.graphics.Color.WHITE,
                        toolbarBackButtonColor = android.graphics.Color.WHITE,
                        toolbarTintColor = android.graphics.Color.WHITE,
                        activityTitle = context.getString(com.example.R.string.cropper_title),
                        cropMenuCropButtonTitle = context.getString(com.example.R.string.cropper_action),
                        activityMenuIconColor = android.graphics.Color.WHITE
                    )
                )
            )
        } catch (e: Exception) {
            photoError = e.localizedMessage ?: "Unable to crop photo."
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            cropPhoto(uri)
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = cameraCaptureUri
        if (success && uri != null) {
            cropPhoto(uri)
        } else if (uri != null) {
            photoError = "The camera did not capture a photo."
        }
    }

    val isReviewingCrop = croppedPhotoUri != null
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isReviewingCrop) Color.Black else AppBackground)
            .navigationBarsPadding()
            .testTag("add_photo_screen")
    ) {
        if (!isReviewingCrop) {
            SoulTopBar(
                title = "Add Photo",
                showBack = true,
                onBackClick = onBack
            )
        }

        if (croppedPhotoUri != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Review your crop",
                    color = Color.White,
                    style = H3Style,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                AsyncImage(
                    model = croppedPhotoUri,
                    contentDescription = "Cropped profile photo preview",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("cropped_photo_preview")
                )

                if (!photoError.isNullOrBlank()) {
                    Text(
                        text = photoError.orEmpty(),
                        color = Color(0xFFFF8A80),
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                GradientButton(
                    text = "Next",
                    onClick = { croppedPhotoUri?.let(onPhotoAdded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                        .testTag("cropped_photo_next")
                )
                SoulTextButton(
                    text = "Choose Another Photo",
                    onClick = {
                        croppedPhotoUri = null
                        photoError = null
                    },
                    color = Color.White,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Choose photo source",
                    style = H3Style,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                if (!photoError.isNullOrBlank()) {
                    Text(
                        text = photoError.orEmpty(),
                        color = Color(0xFFB42318),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                PhotoSourceCard(
                    title = "Choose from Gallery",
                    subtitle = "Pick photos from your device gallery",
                    icon = Icons.Default.PhotoLibrary,
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    tag = "gallery_photo_card"
                )

                Spacer(modifier = Modifier.height(20.dp))

                PhotoSourceCard(
                    title = "Take Photo",
                    subtitle = "Use camera to snap a fresh picture",
                    icon = Icons.Default.CameraAlt,
                    onClick = {
                        photoError = null
                        val uri = context.createCameraCaptureUri("profile_photo")
                        cameraCaptureUri = uri
                        takePictureLauncher.launchCameraCapture(uri) { photoError = it }
                    },
                    tag = "take_photo_card"
                )
            }
        }
    }
}

@Composable
private fun PhotoSourceCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    tag: String
) {
    val cardShape = RoundedCornerShape(24.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(CardBackground)
            .border(1.dp, BorderSubtle, cardShape)
            .clickable(onClick = onClick)
            .padding(24.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(AuroraHorizontalGradient),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.size(20.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontFamily = InterFontFamily,
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
    }
}
