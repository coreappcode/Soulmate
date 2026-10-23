package com.example.ui.screens.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.utils.createCameraCaptureUri
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
import java.io.IOException
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun EditPhotosScreen(
    photos: List<String>,
    onAddPhoto: (String) -> Unit,
    onReplacePhoto: (Int, String) -> Unit,
    onRemovePhoto: (Int) -> Unit,
    onSetMainPhoto: (Int) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    isUploading: Boolean = false,
    errorMessage: String? = null
) {
    var cropError by remember { mutableStateOf<String?>(null) }
    var replacementIndex by remember { mutableStateOf<Int?>(null) }
    var photoToCrop by remember { mutableStateOf<Pair<Int, String>?>(null) }
    var stagedCropUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val cropImageLauncher = rememberLauncherForActivityResult(
        contract = CropImageContract()
    ) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { croppedUri ->
                val photoIndex = replacementIndex
                if (photoIndex != null) {
                    onReplacePhoto(photoIndex, croppedUri.toString())
                    replacementIndex = null
                    cropError = null
                } else if (photos.size < 6) {
                    onAddPhoto(croppedUri.toString())
                    cropError = null
                } else {
                    cropError = "A profile can contain at most six photos."
                }
            } ?: run {
                cropError = "The cropper did not return an image. Please try again."
            }
        } else if (result.error != null) {
            cropError = result.error?.localizedMessage ?: "Unable to crop this photo."
        }
        stagedCropUri?.let { context.contentResolver.delete(it, null, null) }
        stagedCropUri = null
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
                        outputCompressFormat = android.graphics.Bitmap.CompressFormat.JPEG
                    )
                )
            )
        } catch (e: Exception) {
            cropError = e.localizedMessage ?: "Unable to crop photo."
            val index = replacementIndex
            if (index != null) {
                onReplacePhoto(index, uri.toString())
            } else if (photos.size < 6) {
                onAddPhoto(uri.toString())
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            replacementIndex = null
            cropPhoto(uri)
        }
    }

    LaunchedEffect(photoToCrop) {
        val (index, photoUrl) = photoToCrop ?: return@LaunchedEffect
        try {
            val sourceUri = Uri.parse(photoUrl)
            val cropUri = if (sourceUri.scheme == "https") {
                val localUri = context.createCameraCaptureUri("profile_crop")
                withContext(Dispatchers.IO) {
                    val connection = URL(photoUrl).openConnection().apply {
                        connectTimeout = 15_000
                        readTimeout = 30_000
                    }
                    connection.getInputStream().use { input ->
                        val output = context.contentResolver.openOutputStream(localUri)
                            ?: throw IOException("Unable to prepare this photo for cropping.")
                        output.use { input.copyTo(it) }
                    }
                }
                stagedCropUri = localUri
                localUri
            } else if (sourceUri.scheme == "content" || sourceUri.scheme == "file") {
                sourceUri
            } else {
                throw IOException("This photo cannot be cropped from its current location.")
            }
            replacementIndex = index
            cropPhoto(cropUri)
        } catch (exception: Exception) {
            cropError = exception.localizedMessage ?: "Unable to prepare this photo for cropping."
            replacementIndex = null
        } finally {
            photoToCrop = null
        }
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

            if (isUploading || !errorMessage.isNullOrBlank() || !cropError.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = when {
                        isUploading -> "Uploading photo..."
                        !cropError.isNullOrBlank() -> cropError.orEmpty()
                        else -> errorMessage.orEmpty()
                    },
                    fontFamily = InterFontFamily,
                    fontSize = 13.sp,
                    color = if (isUploading) TextSecondary else RosePrimary
                )
            }

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
                                    .clickable { photoToCrop = index to photoUrl }
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
                modifier = Modifier.fillMaxWidth(),
                enabled = photos.size < 6 && !isUploading
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
                enabled = !isUploading,
                loading = isUploading,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
