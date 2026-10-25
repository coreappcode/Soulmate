package com.example.ui.cropper

import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.canhub.cropper.CropImageActivity
import com.example.R

class InsetsAwareCropImageActivity : CropImageActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        supportActionBar?.setHomeActionContentDescription(R.string.cropper_cancel)

        val decorView = window.decorView
        val initialPaddingLeft = decorView.paddingLeft
        val initialPaddingTop = decorView.paddingTop
        val initialPaddingRight = decorView.paddingRight
        val initialPaddingBottom = decorView.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(decorView) { view, windowInsets ->
            val safeInsets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            view.setPadding(
                initialPaddingLeft + safeInsets.left,
                initialPaddingTop + safeInsets.top,
                initialPaddingRight + safeInsets.right,
                initialPaddingBottom + safeInsets.bottom
            )
            WindowInsetsCompat.CONSUMED
        }
        ViewCompat.requestApplyInsets(decorView)
    }
}
