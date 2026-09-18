package com.example.currency.domain.usecase.preferences

import com.example.currency.domain.model.PreferenceUpdate
import com.example.currency.domain.model.QuickCurrencyPolicy
import com.example.currency.domain.repository.UserPreferencesRepository
import javax.inject.Inject

class UpdateUserPreferencesUseCase @Inject constructor(private val repository: UserPreferencesRepository) {
    operator fun invoke(update: PreferenceUpdate) {
        when (update) {
            is PreferenceUpdate.DefaultCurrency -> repository.setDefaultCurrency(update.code)
            is PreferenceUpdate.QuickCurrencies -> repository.setQuickCurrencies(update.codes.take(QuickCurrencyPolicy.MAX_CURRENCIES))
            is PreferenceUpdate.Theme -> repository.setThemeMode(update.mode)
            is PreferenceUpdate.Language -> repository.setLanguageTag(update.tag)
            is PreferenceUpdate.OnboardingCompleted -> repository.setOnboardingCompleted(update.completed)
            is PreferenceUpdate.LastConversionPair -> repository.setLastConversionPair(update.pair)
            is PreferenceUpdate.CompleteOnboarding -> {
                repository.setQuickCurrencies(update.quickCurrencies.take(QuickCurrencyPolicy.MAX_CURRENCIES))
                repository.setOnboardingCompleted(true)
            }
        }
    }
}
