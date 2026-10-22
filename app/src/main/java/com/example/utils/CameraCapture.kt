package com.example.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

fun Context.createCameraCaptureUri(prefix: String): Uri {
    val photoFile = File(cacheDir, "${prefix}_${UUID.randomUUID()}.jpg")
    return FileProvider.getUriForFile(
        this,
        "$packageName.fileprovider",
        photoFile
    )
}
