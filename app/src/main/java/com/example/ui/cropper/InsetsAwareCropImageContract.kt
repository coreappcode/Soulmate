package com.example.ui.cropper

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageView

class InsetsAwareCropImageContract :
    ActivityResultContract<CropImageContractOptions, CropImageView.CropResult>() {
    private val resultParser = CropImageContract()

    override fun createIntent(context: Context, input: CropImageContractOptions): Intent {
        val cropOptions = Bundle().apply {
            putParcelable(CropImage.CROP_IMAGE_EXTRA_SOURCE, input.uri)
            putParcelable(CropImage.CROP_IMAGE_EXTRA_OPTIONS, input.cropImageOptions)
        }
        return Intent(context, InsetsAwareCropImageActivity::class.java)
            .putExtra(CropImage.CROP_IMAGE_EXTRA_BUNDLE, cropOptions)
    }

    override fun parseResult(
        resultCode: Int,
        intent: Intent?
    ): CropImageView.CropResult = resultParser.parseResult(resultCode, intent)
}
