package com.example.currency.presentation.base

import android.content.res.Configuration
import android.view.Window
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

internal fun Window.hideSystemBars() {
    WindowInsetsControllerCompat(this, decorView).apply {
        hide(WindowInsetsCompat.Type.navigationBars())
        show(WindowInsetsCompat.Type.statusBars())
        systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        val isNightMode = (context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        isAppearanceLightStatusBars = !isNightMode
    }
}

internal fun Window.hideSystemBarsAfterImeDismissed() {
    var imeWasVisible = false
    val decor = decorView
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
