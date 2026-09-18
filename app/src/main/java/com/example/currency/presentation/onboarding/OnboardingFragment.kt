package com.example.currency.presentation.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.currency.R
import com.example.currency.databinding.FragmentOnboardingBinding
import com.example.currency.presentation.base.BaseFragment

class OnboardingFragment : BaseFragment<FragmentOnboardingBinding>(FragmentOnboardingBinding::inflate) {

    override fun setUp() {

        val slides = OnboardingSlides.list
        val adapter = OnboardingAdapter(slides)
        binding.viewPagerOnboarding.adapter = adapter

        binding.viewPagerOnboarding.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateDots(position)
                if (position == slides.size - 1) {
                    binding.btnNext.setText(R.string.get_started)
                } else {
                    binding.btnNext.setText(R.string.continue_text)
                }
            }
        })

        binding.btnNext.setOnClickListener {
            val current = binding.viewPagerOnboarding.currentItem
            if (current < slides.size - 1) {
                binding.viewPagerOnboarding.currentItem = current + 1
            } else {
                finishOnboarding()
            }
        }

        binding.btnSkip.setOnClickListener {
            finishOnboarding()
        }
    }

    private fun updateDots(position: Int) {
        val density = resources.displayMetrics.density
        val activeWidth = (24 * density).toInt()
        val inactiveWidth = (6 * density).toInt()
        val dotHeight = (6 * density).toInt()

        val dots = listOf(binding.dot0, binding.dot1, binding.dot2)
        dots.forEachIndexed { index, dotView ->
            if (index == position) {
                dotView.setBackgroundResource(R.drawable.bg_pill_dot_active)
                dotView.layoutParams = LinearLayout.LayoutParams(activeWidth, dotHeight).apply {
                    if (index < dots.size - 1) marginEnd = (6 * density).toInt()
                }
            } else {
                dotView.setBackgroundResource(R.drawable.bg_pill_dot_inactive)
                dotView.layoutParams = LinearLayout.LayoutParams(inactiveWidth, dotHeight).apply {
                    if (index < dots.size - 1) marginEnd = (6 * density).toInt()
                }
            }
        }
    }

    private fun finishOnboarding() {
        findNavController().navigate(R.id.action_onboardingFragment_to_onboardingLanguageFragment)
    }

}
