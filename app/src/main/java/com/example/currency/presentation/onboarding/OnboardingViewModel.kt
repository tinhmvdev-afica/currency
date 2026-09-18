package com.example.currency.presentation.onboarding

import androidx.lifecycle.ViewModel
import com.example.currency.domain.model.PreferenceUpdate
import com.example.currency.domain.model.QuickCurrencyPolicy
import com.example.currency.domain.model.UserPreferences
import com.example.currency.domain.usecase.preferences.GetUserPreferencesUseCase
import com.example.currency.domain.usecase.preferences.UpdateUserPreferencesUseCase
import com.example.currency.presentation.common.locale.LocaleController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val getPreferences: GetUserPreferencesUseCase,
    private val updatePreferences: UpdateUserPreferencesUseCase,
    private val localeController: LocaleController
) : ViewModel() {
    val maxQuickCurrencies: Int get() = QuickCurrencyPolicy.MAX_CURRENCIES

    fun getQuickCurrencies(): List<String> = getPreferences().quickCurrencies

    fun getLanguageTag(): String = getPreferences().languageTag ?: UserPreferences.DEFAULT_LANGUAGE_TAG

    fun setLanguageTag(tag: String) {
        updatePreferences(PreferenceUpdate.Language(tag))
        localeController.apply(tag)
    }

    fun completeOnboarding(currencies: List<String>) {
        updatePreferences(PreferenceUpdate.CompleteOnboarding(currencies))
    }
}
