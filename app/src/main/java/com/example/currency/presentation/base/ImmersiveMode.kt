package com.example.currency.presentation.base

import android.view.Window
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

internal fun Window.hideSystemBars() {
    WindowCompat.setDecorFitsSystemWindows(this, false)
    WindowInsetsControllerCompat(this, decorView).apply {
        hide(WindowInsetsCompat.Type.systemBars())
        systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

internal fun Window.hideSystemBarsAfterImeDismissed() {
    var imeWasVisible = false
    val decor = this.decorView
    ViewCompat.setOnApplyWindowInsetsListener(decor) { view, insets ->
        val imeIsVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
        if (imeWasVisible && !imeIsVisible) {
            view.post { hideSystemBars() }
        }
        imeWasVisible = imeIsVisible
        insets
    }
    ViewCompat.requestApplyInsets(decor)
}
