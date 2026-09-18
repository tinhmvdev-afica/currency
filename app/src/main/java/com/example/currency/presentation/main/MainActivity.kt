package com.example.currency.presentation.main

import com.example.currency.databinding.ActivityMainBinding
import com.example.currency.presentation.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun inflateBinding(inflater: android.view.LayoutInflater): ActivityMainBinding =
        ActivityMainBinding.inflate(inflater)

}
