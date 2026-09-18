package com.example.currency.presentation.common.locale

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import javax.inject.Inject

class LocaleController @Inject constructor() {
    fun apply(tag: String) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
    }
}
