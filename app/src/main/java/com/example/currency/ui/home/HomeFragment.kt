package com.example.currency.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        // Bottom Navigation listener
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_converter -> {
                    binding.viewPagerMain.setCurrentItem(0, false)
                    true
                }
                R.id.nav_markets -> {
                    binding.viewPagerMain.setCurrentItem(1, false)
                    true
                }
                R.id.nav_settings -> {
                    binding.viewPagerMain.setCurrentItem(2, false)
                    true
                }
                else -> false
            }
        }

        binding.viewPagerMain.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val menuId = when (position) {
                    0 -> R.id.nav_converter
                    1 -> R.id.nav_markets
                    2 -> R.id.nav_settings
                    else -> R.id.nav_converter
                }
                if (binding.bottomNavigation.selectedItemId != menuId) {
                    binding.bottomNavigation.selectedItemId = menuId
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}