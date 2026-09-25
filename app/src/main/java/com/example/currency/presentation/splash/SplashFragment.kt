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
import com.example.currency.ads.AppOpenAdManager
import com.example.currency.databinding.FragmentSplashBinding
import com.example.currency.presentation.base.BaseFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.currency.presentation.shared.CoinViewModel
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAd
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue
@AndroidEntryPoint
class SplashFragment : BaseFragment<FragmentSplashBinding>(FragmentSplashBinding::inflate) {

    private val viewModel: CoinViewModel by activityViewModels()
    private val splashViewModel: SplashViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!splashViewModel.isOnboardingCompleted) {
            findNavController().navigate(
                R.id.action_splashFragment_to_onboardingFragment
            )
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
//            AppOpenAdManager.waitForAd()
            delay(2000)
            if (isAdded) {
                if (splashViewModel.isOnboardingCompleted) {
                    findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
                } else {
                    findNavController().navigate(R.id.action_splashFragment_to_onboardingFragment)
                }
            }

        }
    }

    override fun setUp() {
        // Resolve the activity-scoped ViewModel. Its init block starts the initial data load.
        viewModel.uiState
//        viewLifecycleOwner.lifecycleScope.launch {
//            delay(1_500)
//            if (!isAdded)
//            return@launch
//
//            if (!splashViewModel.isOnboardingCompleted) {
//                findNavController().navigate(
//                    R.id.action_splashFragment_to_onboardingFragment
//                )
//                return@launch
//            }
//            AppOpenAdManager.showAdIfAvailable(requireActivity()){
//                if(isAdded){
//                    findNavController().navigate(
//                        R.id.action_splashFragment_to_homeFragment
//                    )
//                }
//            }

    }
}
