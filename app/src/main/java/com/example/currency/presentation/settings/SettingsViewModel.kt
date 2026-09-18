package com.example.currency.presentation.settings

import androidx.lifecycle.ViewModel
import com.example.currency.domain.model.PreferenceUpdate
import com.example.currency.domain.model.QuickCurrencyPolicy
import com.example.currency.domain.model.ThemeMode
import com.example.currency.domain.model.UserPreferences
import com.example.currency.domain.usecase.preferences.GetUserPreferencesUseCase
import com.example.currency.domain.usecase.preferences.UpdateUserPreferencesUseCase
import com.example.currency.presentation.common.locale.LocaleController
import com.example.currency.presentation.common.theme.ThemeController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getPreferences: GetUserPreferencesUseCase,
    private val updatePreferences: UpdateUserPreferencesUseCase,
    private val themeController: ThemeController,
    private val localeController: LocaleController
) : ViewModel() {
    val maxQuickCurrencies: Int get() = QuickCurrencyPolicy.MAX_CURRENCIES

    fun getQuickCurrencies(): List<String> = getPreferences().quickCurrencies

    fun setQuickCurrencies(currencies: List<String>) {
        updatePreferences(PreferenceUpdate.QuickCurrencies(currencies))
    }

    fun getThemeMode(): ThemeMode = getPreferences().themeMode

    fun setThemeMode(mode: ThemeMode) {
        if (getThemeMode() == mode) return
        updatePreferences(PreferenceUpdate.Theme(mode))
        themeController.apply(mode)
    }

    fun getLanguageTag(): String = getPreferences().languageTag ?: UserPreferences.DEFAULT_LANGUAGE_TAG

    fun setLanguageTag(tag: String) {
        updatePreferences(PreferenceUpdate.Language(tag))
        localeController.apply(tag)
    }
}
