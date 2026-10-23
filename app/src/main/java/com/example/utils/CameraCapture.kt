package com.example.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

fun ActivityResultLauncher<Uri>.launchCameraCapture(
    uri: Uri,
    onError: (String) -> Unit
) {
    try {
        launch(uri)
    } catch (_: ActivityNotFoundException) {
        onError("No camera app is available on this device.")
    } catch (_: SecurityException) {
        onError("Camera access was denied. Check your device's camera settings and try again.")
    }
}

fun Context.createCameraCaptureUri(prefix: String): Uri {
    val photoFile = File(cacheDir, "${prefix}_${UUID.randomUUID()}.jpg")
    return FileProvider.getUriForFile(
        this,
        "$packageName.fileprovider",
        photoFile
    )
}
