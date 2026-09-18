package com.example.currency.domain.usecase.preferences

import com.example.currency.domain.model.UserPreferences
import com.example.currency.domain.repository.UserPreferencesRepository
import javax.inject.Inject

class GetUserPreferencesUseCase @Inject constructor(private val repository: UserPreferencesRepository) {
    operator fun invoke() = UserPreferences(
        onboardingCompleted = repository.isOnboardingCompleted(),
        defaultCurrency = repository.getDefaultCurrency(),
        quickCurrencies = repository.getQuickCurrencies(),
        themeMode = repository.getThemeMode(),
        languageTag = repository.getLanguageTag(),
        lastConversionPair = repository.getLastConversionPair()
    )
}
