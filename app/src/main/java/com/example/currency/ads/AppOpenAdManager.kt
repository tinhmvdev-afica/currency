package com.example.currency.ads

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAd
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull


object AppOpenAdManager {
    private const val TAG = "AppOpenAd"
    private const val AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"

    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    private var isShowingAd = false
    private var loadTime = 0L
    private val mainHandler = Handler(Looper.getMainLooper())
    private val adLoadResult = MutableStateFlow<Boolean?>(null)



    fun loadAd() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post { loadAd() }
            return
        }
        if (isLoadingAd || isShowingAd || isAdAvailable()) return

        // A cached app-open ad cannot be reused after four hours.
        appOpenAd?.destroy()
        appOpenAd = null
        loadTime = 0L

        isLoadingAd = true
        adLoadResult.value = null

        val request = AdRequest.Builder(AD_UNIT_ID).build()

        val start = SystemClock.elapsedRealtime()
        Log.d("AdsTiming", "Bắt đầu load ad")
        AppOpenAd.load(request, object : AdLoadCallback<AppOpenAd> {
            override fun onAdLoaded(ad: AppOpenAd) {
                mainHandler.post {
                    Log.d(TAG, "APP_OPEN: loaded")
                    appOpenAd = ad
                    loadTime = SystemClock.elapsedRealtime()
                    isLoadingAd = false
                    adLoadResult.value = true
                }
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                mainHandler.post {
                    Log.d(TAG, "APP_OPEN: failed ${adError.message}")
                    appOpenAd = null
                    isLoadingAd = false
                    adLoadResult.value = false
                }
            }
        })
    }

    suspend fun waitForAd(): Boolean =
        withTimeoutOrNull(15_000L) {
            adLoadResult.filterNotNull().first()
        } == true
    fun showAdIfAvailable(
        activity: Activity,
//        onComplete:() -> Unit
    ) {
        Log.d(TAG,"APP_OPEN: show called, ad = $appOpenAd")
        if (isShowingAd){
//            onComplete()
            return
        }

        val ad = appOpenAd?.takeIf { isAdAvailable() } ?: run {
            loadAd()
//            onComplete()
            return
        }

        ad.adEventCallback = object : AppOpenAdEventCallback {
            override fun onAdDismissedFullScreenContent() {
                mainHandler.post {
                    isShowingAd = false
                    appOpenAd = null
                    isLoadingAd = false
                    loadAd()
//                    onComplete()
                }
            }

            override fun onAdFailedToShowFullScreenContent(error: FullScreenContentError) {
                mainHandler.post {
                    appOpenAd = null
                    isShowingAd = false
                    isLoadingAd = false
                    loadAd()
//                    onComplete()
                }
            }
        }

        isShowingAd = true
        ad.show(activity)
    }

    private fun isAdAvailable(): Boolean =
        appOpenAd != null && wasLoadedLessThanFourHoursAgo()

    private fun wasLoadedLessThanFourHoursAgo(): Boolean {
        val fourHoursInMillis = 4 * 60 * 60 * 1000L
        return loadTime != 0L && SystemClock.elapsedRealtime() - loadTime < fourHoursInMillis
    }
}
