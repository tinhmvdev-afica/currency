package com.example.currency.ui.splash

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.currency.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.currency.data.local.PreferencesHelper
import com.example.currency.viewmodel.CoinViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue
@AndroidEntryPoint
class SplashFragment : Fragment(R.layout.fragment_splash) {

    private val viewModel: CoinViewModel by activityViewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadCoins("usd")
        viewModel.loadFiats()

        viewLifecycleOwner.lifecycleScope.launch {
            delay(5000)
            if (isAdded) {
                if (PreferencesHelper.isOnboardingCompleted(requireContext())) {
                    findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
                } else {
                    findNavController().navigate(R.id.action_splashFragment_to_onboardingFragment)
                }
            }
        }
    }
}