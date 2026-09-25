package com.example.currency.presentation.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.viewbinding.ViewBinding
import com.example.currency.ads.AppOpenAdManager
import java.lang.ref.WeakReference

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {
    private var _binding: VB? = null
    protected val binding: VB get() = checkNotNull(_binding) { "Binding is unavailable" }

    protected abstract fun inflateBinding(inflater: LayoutInflater): VB
    open val isExcludedFromAppOpenAd: Boolean = false
    companion object {
        private var isObserverRegistered = false
        private var hasStartedOnce = false
        private var currentActivityRef: WeakReference<BaseActivity<*>>? = null
        private var timeBackgrounded: Long = 0
        private const val MIN_BACKGROUND_TIME = 30_000L

        private fun registerAppOpenAdObserverOnce() {
            if (isObserverRegistered) return
            isObserverRegistered = true

            ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onStop(owner: LifecycleOwner) {
                    // App thực sự xuống background -> lưu lại mốc thời gian
                    timeBackgrounded = System.currentTimeMillis()
                }
                override fun onStart(owner: LifecycleOwner) {
                    // SplashFragment handles a cold start. This observer handles returns from background.
                    if (!hasStartedOnce) {
                        hasStartedOnce = true
                        return
                    }
                    val timeAway = System.currentTimeMillis() - timeBackgrounded
                    // Chỉ show khi ở ngoài đủ 30 giây trở lên
                    if (timeBackgrounded == 0L || timeAway < MIN_BACKGROUND_TIME) return

                    val activity = currentActivityRef?.get() ?: return
                    if (activity.isExcludedFromAppOpenAd) return

                    AppOpenAdManager.showAdIfAvailable(activity)
                }
            })
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = inflateBinding(layoutInflater)

        setContentView(binding.root)
        window.hideSystemBarsAfterImeDismissed()
        window.hideSystemBars()
        registerAppOpenAdObserverOnce()

    }

    override fun onStart() {
        super.onStart()
        currentActivityRef = WeakReference(this)
    }

    override fun onResume() {
        super.onResume()
        currentActivityRef = WeakReference(this)
        window.hideSystemBars()
        window.decorView.postDelayed({ window.hideSystemBars() }, 1_000)
    }

    override fun onStop() {

        super.onStop()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) window.hideSystemBars()
    }


    override fun onDestroy() {
        if (currentActivityRef?.get() === this) {
            currentActivityRef = null
        }
        _binding = null
        super.onDestroy()
    }
}
