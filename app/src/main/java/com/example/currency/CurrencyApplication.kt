package com.example.currency

import android.app.Application
import android.os.SystemClock
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.example.currency.ads.AppOpenAdManager
import com.example.currency.domain.model.UserPreferences
import com.example.currency.domain.repository.UserPreferencesRepository
import com.example.currency.presentation.common.locale.LocaleController
import com.example.currency.presentation.common.theme.ThemeController
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
        CoroutineScope(Dispatchers.IO).launch {
            val start = SystemClock.elapsedRealtime()
            Log.d("AdsTiming", "Bắt đầu initialize")
            MobileAds.initialize(
                this@CurrencyApplication,
                InitializationConfig.Builder(
                    "ca-app-pub-3940256099942544~3347511713"

                ).build()
            ){
                Log.d(
                    "AdsTiming",
                    "Initialize xong sau ${SystemClock.elapsedRealtime() - start} ms"
                )
                println("Ads initialized")
                AppOpenAdManager.loadAd()
            }

        }


    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            // CoinGecko không gửi Cache-Control. Vẫn lưu icon vào disk cache để dùng lại offline.
            .respectCacheHeaders(false)
            .build()
    }
}
