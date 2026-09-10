package com.example.currency

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory

/** Cấu hình Coil dùng chung cho toàn bộ AsyncImage trong ứng dụng. */
class CurrencyApplication : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            // CoinGecko không gửi Cache-Control. Vẫn lưu icon vào disk cache để dùng lại offline.
            .respectCacheHeaders(false)
            .build()
    }
}