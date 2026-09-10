package com.example.currency.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import com.example.currency.R
import com.example.currency.data.local.PreferencesHelper
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

        // Setup Language Section
        setupLanguageSection(prefs)

        // Setup Quick Multi-Currencies
        setupQuickCurrencies()

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
            Toast.makeText(
                requireContext(),
                getString(R.string.default_currency_saved, nextCurrency),
                Toast.LENGTH_SHORT
            ).show()
        }

        // Auto Refresh switch
        val autoRefresh = prefs.getBoolean("auto_refresh", true)
        binding.switchAutoRefresh.isChecked = autoRefresh
        binding.switchAutoRefresh.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("auto_refresh", isChecked).apply()
            val msg = getString(if (isChecked) R.string.auto_refresh_enabled else R.string.auto_refresh_disabled)
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupQuickCurrencies() {
        updateQuickCurrenciesBadge()

        binding.btnQuickCurrencies.setOnClickListener {
            val bottomSheet = QuickCurrenciesBottomSheet {
                updateQuickCurrenciesBadge()
            }
            bottomSheet.show(parentFragmentManager, "QuickCurrenciesBottomSheet")
        }
    }

    private fun updateQuickCurrenciesBadge() {
        val count = PreferencesHelper.getQuickCurrencies(requireContext()).size
        binding.tvQuickCurrenciesBadge.text = "$count/${PreferencesHelper.MAX_QUICK_CURRENCIES} ›"
    }

    private fun setupLanguageSection(prefs: SharedPreferences) {
        val selectedTag = prefs.getString("app_language", "en") ?: "en"
        updateActiveLanguageUI(selectedTag)

        // Open BottomSheet when clicking language row
        binding.btnLanguagePicker.setOnClickListener {
            val bottomSheet = LanguagePickerBottomSheet(selectedTag) { newLanguage ->
                applyLanguage(prefs, newLanguage)
            }
            bottomSheet.show(parentFragmentManager, "LanguagePickerBottomSheet")
        }
    }

    private fun updateActiveLanguageUI(selectedTag: String) {
        val langItem = LanguagePickerBottomSheet.ALL_LANGUAGES.firstOrNull {
            it.tag.equals(selectedTag, ignoreCase = true)
        } ?: LanguagePickerBottomSheet.ALL_LANGUAGES.first { it.tag == "en" }

        binding.tvActiveLanguageFlag.text = langItem.flagEmoji
        binding.tvActiveLanguageName.text = langItem.displayName
        binding.tvAllLanguagesBadge.text = getString(R.string.select_language)
    }

    private fun applyLanguage(prefs: SharedPreferences, language: LanguageItem) {
        prefs.edit().putString("app_language", language.tag).apply()
        Toast.makeText(
            requireContext(),
            getString(R.string.language_updated, language.displayName),
            Toast.LENGTH_SHORT
        ).show()

        // Set application locale - this automatically recreates the activity and applies new locale
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.tag))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
