package com.example.currency.presentation.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.example.currency.R
import com.example.currency.databinding.FragmentSettingsBinding
import com.example.currency.domain.model.ThemeMode
import com.example.currency.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : BaseFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {

    private val settingsViewModel: SettingsViewModel by activityViewModels()

    override fun setUp() {
        // Setup Language Section
        setupLanguageSection()
        // Setup Quick Multi-Currencies
        setupQuickCurrencies()
        // Setup Theme / Appearance
        setupThemeSection()


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
        val count = settingsViewModel.getQuickCurrencies().size
        binding.tvQuickCurrenciesBadge.text = getString(R.string.quick_currencies_badge, count)
    }

    private fun setupLanguageSection() {
        updateActiveLanguageUI(settingsViewModel.getLanguageTag())

        // Open BottomSheet when clicking language row
        binding.btnLanguagePicker.setOnClickListener {
            val bottomSheet = LanguagePickerBottomSheet(settingsViewModel.getLanguageTag()) { newLanguage ->
                applyLanguage(newLanguage)
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

    private fun applyLanguage(language: LanguageItem) {
        Toast.makeText(
            requireContext(),
            getString(R.string.language_updated, language.displayName),
            Toast.LENGTH_SHORT
        ).show()

        settingsViewModel.setLanguageTag(language.tag)
    }

    private fun setupThemeSection() {
        val currentTheme = settingsViewModel.getThemeMode()
        updateThemeUI(currentTheme)

        binding.btnThemePicker.setOnClickListener {
            val themeDialog = ThemePickerDialogFragment(
                currentTheme = settingsViewModel.getThemeMode()
            ) { newTheme ->
                onThemeOptionSelected(newTheme)
            }
            themeDialog.show(parentFragmentManager, "ThemePickerDialogFragment")
        }
    }

    private fun onThemeOptionSelected(newTheme: ThemeMode) {
        val currentTheme = settingsViewModel.getThemeMode()
        if (currentTheme == newTheme) return

        updateThemeUI(newTheme)
        settingsViewModel.setThemeMode(newTheme)
    }

    private fun updateThemeUI(selectedTheme: ThemeMode) {
        val (iconRes, nameRes) = when (selectedTheme) {
            ThemeMode.LIGHT -> Pair(R.drawable.ic_theme_light, R.string.theme_light)
            ThemeMode.DARK -> Pair(R.drawable.ic_theme_dark, R.string.theme_dark)
            ThemeMode.SYSTEM -> Pair(R.drawable.ic_theme_system, R.string.theme_system)
        }

        binding.ivActiveThemeIcon.setImageResource(iconRes)
        val themeName = getString(nameRes)
        binding.tvActiveThemeName.text = themeName
        binding.tvThemeBadge.text = themeName
    }

}
