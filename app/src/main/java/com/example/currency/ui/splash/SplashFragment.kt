package com.example.currency.ui.splash

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.currency.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment(R.layout.fragment_splash) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            delay(1500)
            if (isAdded) {
                try {
                    findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
                } catch (e: Exception) {
                    findNavController().navigate(R.id.homeFragment)
                }
            }
        }
    }
}