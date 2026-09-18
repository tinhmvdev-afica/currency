package com.example.currency

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.example.currency.domain.model.UserPreferences
import com.example.currency.domain.repository.UserPreferencesRepository
import com.example.currency.presentation.common.locale.LocaleController
import com.example.currency.presentation.common.theme.ThemeController
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject


/** Cấu hình Coil dùng chung cho toàn bộ AsyncImage trong ứng dụng. */
@HiltAndroidApp
class CurrencyApplication : Application(), ImageLoaderFactory {
    @Inject lateinit var preferences: UserPreferencesRepository
    @Inject lateinit var themeController: ThemeController
    @Inject lateinit var localeController: LocaleController

    override fun onCreate() {
        super.onCreate()
        themeController.apply(preferences.getThemeMode())
        localeController.apply(preferences.getLanguageTag() ?: UserPreferences.DEFAULT_LANGUAGE_TAG)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            // CoinGecko không gửi Cache-Control. Vẫn lưu icon vào disk cache để dùng lại offline.
            .respectCacheHeaders(false)
            .build()
    }
}
