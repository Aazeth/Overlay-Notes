package com.example.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat

fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@Composable
fun AdaptStatusBarColor(headerColor: Color) {
    val context = LocalContext.current
    val view = LocalView.current
    if (!view.isInEditMode) {
        DisposableEffect(headerColor) {
            val activity = context.findActivity() ?: view.context.findActivity()
            val window = activity?.window
            val insetsController = if (window != null) WindowCompat.getInsetsController(window, view) else null
            val originalColor = window?.statusBarColor
            val originalLightStatusBars = insetsController?.isAppearanceLightStatusBars

            if (window != null) {
                val colorArgb = headerColor.toArgb()
                window.statusBarColor = colorArgb
                val isLightHeader = ColorUtils.calculateLuminance(colorArgb) > 0.5
                insetsController?.isAppearanceLightStatusBars = isLightHeader
            }

            onDispose {
                if (window != null && originalColor != null && originalLightStatusBars != null) {
                    window.statusBarColor = originalColor
                    insetsController?.isAppearanceLightStatusBars = originalLightStatusBars
                }
            }
        }
    }
}
