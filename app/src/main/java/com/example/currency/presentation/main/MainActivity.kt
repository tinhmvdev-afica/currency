package com.example.currency.presentation.main

import com.example.currency.ads.AppOpenAdManager
import com.example.currency.databinding.ActivityMainBinding
import com.example.currency.presentation.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {
    private var timeBackgrounded: Long = 0
    private val MIN_BACKGROUND_TIME = 30_000L

    override fun inflateBinding(inflater: android.view.LayoutInflater): ActivityMainBinding =
        ActivityMainBinding.inflate(inflater)

    override fun onStop() {
        super.onStop()
        // Ghi lại thời điểm app bắt đầu bị ẩn xuống
//        timeBackgrounded = System.currentTimeMillis()
    }

    override fun onStart() {
        super.onStart()
//        val timeAway = System.currentTimeMillis() - timeBackgrounded
//        if (timeBackgrounded != 0L && timeAway >= MIN_BACKGROUND_TIME) {
//            AppOpenAdManager.showAdIfAvailable(this, onComplete = {})
//        }
    }

}
