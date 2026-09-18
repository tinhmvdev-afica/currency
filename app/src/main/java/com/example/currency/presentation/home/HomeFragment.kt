package com.example.currency.presentation.home

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.currency.R
import com.example.currency.databinding.FragmentHomeBinding
import com.example.currency.presentation.base.BaseFragment
import com.example.currency.presentation.converter.ConverterFragment
import com.example.currency.presentation.markets.MarketsFragment
import com.example.currency.presentation.settings.SettingsFragment

class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    override fun setUp() {
        // Setup ViewPager2 with 3 tabs
        binding.viewPagerMain.isUserInputEnabled = false
        binding.viewPagerMain.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 3

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> ConverterFragment()
                    1 -> MarketsFragment()
                    2 -> SettingsFragment()
                    else -> ConverterFragment()
                }
            }
        }

        binding.tabConverter.setOnClickListener { selectTab(0) }
        binding.tabMarkets.setOnClickListener { selectTab(1) }
        binding.tabSettings.setOnClickListener { selectTab(2) }
        updateSelectedTab(0)

        binding.viewPagerMain.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateSelectedTab(position)
            }
        })
    }

    private fun selectTab(position: Int) {
        binding.viewPagerMain.setCurrentItem(position, false)
        updateSelectedTab(position)
    }

    private fun updateSelectedTab(selectedPosition: Int) {
        val tabs = listOf(binding.tabConverter, binding.tabMarkets, binding.tabSettings)
        val icons = listOf(binding.iconConverter, binding.iconMarkets, binding.iconSettings)
        val labels = listOf(binding.textConverter, binding.textMarkets, binding.textSettings)

        tabs.forEachIndexed { index, tab -> tab.isSelected = index == selectedPosition }
        icons.forEachIndexed { index, icon -> icon.setColorFilter(tabColor(index == selectedPosition)) }
        labels.forEachIndexed { index, label -> label.setTextColor(tabColor(index == selectedPosition)) }
    }

    private fun tabColor(isSelected: Boolean): Int = ContextCompat.getColor(
        requireContext(),
        if (isSelected) R.color.brand_accent else R.color.content_secondary
    )

}
