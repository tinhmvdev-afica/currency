package com.example.currency.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.currency.R
import com.example.currency.databinding.FragmentHomeBinding
import com.example.currency.ui.converter.ConverterFragment
import com.example.currency.ui.markets.MarketsFragment
import com.example.currency.ui.settings.SettingsFragment

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
