package com.example.currency.presentation.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {
    private var _binding: VB? = null
    protected val binding: VB get() = checkNotNull(_binding) { "Binding is unavailable" }

    protected abstract fun inflateBinding(inflater: LayoutInflater): VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = inflateBinding(layoutInflater)
        setContentView(binding.root)
        window.hideSystemBars()
        window.hideSystemBarsAfterImeDismissed()
    }

    override fun onResume() {
        super.onResume()
        window.hideSystemBars()
        window.decorView.postDelayed({ window.hideSystemBars() }, 1_000)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) window.hideSystemBars()
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}
