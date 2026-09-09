package com.example.currency.ui.settings

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.currency.R
import com.example.currency.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("coinflux_prefs", Context.MODE_PRIVATE)

        // Load default currency
        val currentDefault = prefs.getString("default_currency", "VND (₫)") ?: "VND (₫)"
        binding.tvDefaultCurrencyBadge.text = currentDefault

        // Cycle through currencies when clicked
        val currencies = listOf("VND (₫)", "USD ($)", "EUR (€)", "JPY (¥)")
        binding.btnDefaultCurrency.setOnClickListener {
            val currentIdx = currencies.indexOf(binding.tvDefaultCurrencyBadge.text.toString())
            val nextIdx = (currentIdx + 1) % currencies.size
            val nextCurrency = currencies[nextIdx]
            binding.tvDefaultCurrencyBadge.text = nextCurrency
            prefs.edit().putString("default_currency", nextCurrency).apply()
            Toast.makeText(requireContext(), "Đã đặt đồng tiền mặc định: $nextCurrency", Toast.LENGTH_SHORT).show()
        }

        // Auto Refresh switch
        val autoRefresh = prefs.getBoolean("auto_refresh", true)
        binding.switchAutoRefresh.isChecked = autoRefresh
        binding.switchAutoRefresh.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("auto_refresh", isChecked).apply()
            val msg = if (isChecked) "Đã bật tự động làm mới" else "Đã tắt tự động làm mới"
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }

        // API Key
        val apiKey = prefs.getString("coingecko_api_key", "") ?: ""
        binding.etApiKey.setText(apiKey)
        binding.etApiKey.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                prefs.edit().putString("coingecko_api_key", s.toString().trim()).apply()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Reset Onboarding Button
        binding.btnResetOnboarding.setOnClickListener {
            prefs.edit().putBoolean("onboarding_completed", false).apply()
            Toast.makeText(requireContext(), "Đã đặt lại Onboarding!", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_homeFragment_to_onboardingFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
