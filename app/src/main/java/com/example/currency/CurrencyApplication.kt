package com.example.currency

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.example.currency.data.local.PreferencesHelper
import dagger.hilt.android.HiltAndroidApp


/** Cấu hình Coil dùng chung cho toàn bộ AsyncImage trong ứng dụng. */
@HiltAndroidApp
class CurrencyApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        val themeMode = PreferencesHelper.getThemeMode(this)
        PreferencesHelper.applyTheme(themeMode)

        val languageTag = getSharedPreferences("coinflux_prefs", Context.MODE_PRIVATE)
            .getString("app_language", "en") ?: "en"
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageTag))
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            // CoinGecko không gửi Cache-Control. Vẫn lưu icon vào disk cache để dùng lại offline.
            .respectCacheHeaders(false)
            .build()
    }
}
