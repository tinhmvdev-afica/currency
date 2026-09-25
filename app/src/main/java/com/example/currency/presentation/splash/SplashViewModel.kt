package com.example.currency.presentation.splash

import androidx.lifecycle.ViewModel
import com.example.currency.domain.usecase.preferences.GetUserPreferencesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getPreferences: GetUserPreferencesUseCase
) : ViewModel() {
    val isOnboardingCompleted: Boolean
        get() = getPreferences().onboardingCompleted

}
