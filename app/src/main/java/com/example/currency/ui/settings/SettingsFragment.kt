package com.example.currency.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
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

    private fun setupThemeSection() {
        val currentTheme = PreferencesHelper.getThemeMode(requireContext())
        updateThemeUI(currentTheme)

        binding.btnThemePicker.setOnClickListener {
            val themeDialog = ThemePickerDialogFragment(
                currentTheme = PreferencesHelper.getThemeMode(requireContext())
            ) { newTheme ->
                onThemeOptionSelected(newTheme)
            }
            themeDialog.show(parentFragmentManager, "ThemePickerDialogFragment")
        }
    }

    private fun onThemeOptionSelected(newTheme: String) {
        val currentTheme = PreferencesHelper.getThemeMode(requireContext())
        if (currentTheme == newTheme) return

        PreferencesHelper.setThemeMode(requireContext(), newTheme)
        PreferencesHelper.applyTheme(newTheme)
        updateThemeUI(newTheme)
    }

    private fun updateThemeUI(selectedTheme: String) {
        val (iconRes, nameRes) = when (selectedTheme) {
            PreferencesHelper.THEME_LIGHT -> Pair(R.drawable.ic_theme_light, R.string.theme_light)
            PreferencesHelper.THEME_DARK -> Pair(R.drawable.ic_theme_dark, R.string.theme_dark)
            else -> Pair(R.drawable.ic_theme_system, R.string.theme_system)
        }

        binding.ivActiveThemeIcon.setImageResource(iconRes)
        val themeName = getString(nameRes)
        binding.tvActiveThemeName.text = themeName
        binding.tvThemeBadge.text = themeName
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
