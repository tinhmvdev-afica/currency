package com.example.currency.presentation.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.currency.R
import com.example.currency.databinding.FragmentSplashBinding
import com.example.currency.presentation.base.BaseFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.currency.presentation.shared.CoinViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue
import kotlin.time.Duration.Companion.milliseconds

@AndroidEntryPoint
class SplashFragment : BaseFragment<FragmentSplashBinding>(FragmentSplashBinding::inflate) {

    private val viewModel: CoinViewModel by activityViewModels()
    private val splashViewModel: SplashViewModel by viewModels()

    override fun setUp() {

        // Resolve the activity-scoped ViewModel. Its init block starts the initial data load.
        viewModel.uiState

        viewLifecycleOwner.lifecycleScope.launch {
            delay(2000.milliseconds)
            if (isAdded) {
                if (splashViewModel.isOnboardingCompleted) {
                    findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
                } else {
                    findNavController().navigate(R.id.action_splashFragment_to_onboardingFragment)
                }
            }
        }
    }
}
