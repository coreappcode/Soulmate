package com.example.ui.screens.chats

import android.net.Uri
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.GradientButton
import com.example.ui.components.SoulTextField
import com.example.ui.components.SoulTopBar
import com.example.ui.theme.AppBackground
import com.example.ui.theme.CardBackground
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.OrchidPrimary
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.RosePrimary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.utils.createCameraCaptureUri
import com.example.utils.launchCameraCapture

@Composable
fun SendAttachmentScreen(
    onSendAttachment: (uri: Uri, caption: String, mimeType: String) -> Unit,
    onBack: () -> Unit
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedMimeType by remember { mutableStateOf<String?>(null) }
    var cameraCaptureUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var captionText by remember { mutableStateOf("") }
    var actionError by remember { mutableStateOf<String?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            selectedMimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = cameraCaptureUri
        if (success && uri != null) {
            selectedImageUri = uri
            selectedMimeType = "image/jpeg"
        }
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            selectedMimeType = context.contentResolver.getType(uri) ?: "audio/*"
        }
    }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            selectedMimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
        }
    }

    fun sendSelectedAttachment() {
        val uri = selectedImageUri ?: return
        val mimeType = selectedMimeType ?: return
        onSendAttachment(uri, captionText, mimeType)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("send_attachment_screen")
    ) {
        SoulTopBar(
            title = "Send Attachment",
            showBack = true,
            onBackClick = onBack
        )

        if (!actionError.isNullOrBlank()) {
            Text(
                text = actionError.orEmpty(),
                color = RosePrimary,
                fontFamily = InterFontFamily,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (selectedImageUri != null) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(3f / 3.5f)
                        .border(2.dp, Color(0xFF000000), RoundedCornerShape(20.dp))
                ) {
                    if (selectedMimeType?.startsWith("image/") == true) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (selectedMimeType?.startsWith("audio/") == true) {
                                    Icons.Default.Headphones
                                } else {
                                    Icons.Default.Description
                                },
                                contentDescription = null,
                                tint = OrchidPrimary,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = selectedMimeType.orEmpty(),
                                fontFamily = InterFontFamily,
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                SoulTextField(
                    label = "Caption",
                    value = captionText,
                    onValueChange = { captionText = it },
                    placeholder = "Add a caption..."
                )
            } else {
                Text(
                    text = "Select what you want to share",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Choose from your device library or share a voice note.",
                    fontFamily = InterFontFamily,
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(30.dp))

                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    AttachmentOptionCard(
                        icon = Icons.Default.PhotoLibrary,
                        title = "Photo Gallery",
                        description = "Choose pictures from your camera roll",
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )

                    AttachmentOptionCard(
                        icon = Icons.Default.CameraAlt,
                        title = "Take Instant Photo",
                        description = "Capture an authentic live moment",
                        onClick = {
                            actionError = null
                            val uri = context.createCameraCaptureUri("chat_attachment")
                            cameraCaptureUri = uri
                            takePictureLauncher.launchCameraCapture(uri) { actionError = it }
                        }
                    )

                    AttachmentOptionCard(
                        icon = Icons.Default.Headphones,
                        title = "Audio Note",
                        description = "Share a music snippet or voice track",
                        onClick = {
                            audioPickerLauncher.launch("audio/*")
                        }
                    )

                    AttachmentOptionCard(
                        icon = Icons.Default.Description,
                        title = "Document",
                        description = "Share PDF or event tickets",
                        onClick = {
                            documentPickerLauncher.launch(
                                arrayOf("application/pdf", "text/plain", "application/msword")
                            )
                        }
                    )
                }
            }
        }

        if (selectedImageUri != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppBackground)
                    .border(width = 1.dp, color = Color(0xFF000000).copy(alpha = 0.08f))
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                GradientButton(
                    text = when {
                        selectedMimeType?.startsWith("image/") == true -> "Send Photo"
                        selectedMimeType?.startsWith("audio/") == true -> "Send Audio"
                        else -> "Send Document"
                    },
                    onClick = ::sendSelectedAttachment,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun AttachmentOptionCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, Color(0xFF000000), RoundedCornerShape(18.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(ElevatedSurface)
                    .border(1.dp, Color(0xFF000000), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = OrchidPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
                Text(
                    text = description,
                    fontFamily = InterFontFamily,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
