package com.example.currency

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.currency.data.local.preferences.UserPreferencesRepositoryImpl
import com.example.currency.domain.model.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserPreferencesRepositoryImplTest {
    @Test
    fun readsExistingPreferenceKeysAndDefaults() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = context.getSharedPreferences("coinflux_prefs", Context.MODE_PRIVATE)
        val previous = prefs.all.toMap()
        try {
            prefs.edit().clear().commit()
            val repository = UserPreferencesRepositoryImpl(context)
            assertFalse(repository.isOnboardingCompleted())
            assertEquals("VND", repository.getDefaultCurrency())
            assertEquals(ThemeMode.DARK, repository.getThemeMode())
            assertEquals(listOf("USD", "EUR", "ETH", "SOL", "VND"), repository.getQuickCurrencies())

            repository.setOnboardingCompleted(true)
            repository.setThemeMode(ThemeMode.SYSTEM)
            repository.setLanguageTag("vi")
            repository.setQuickCurrencies(listOf("USD", "EUR", "VND", "BTC", "ETH", "JPY"))
            assertTrue(prefs.getBoolean("is_onboarding_completed", false))
            assertEquals("system", prefs.getString("theme_mode", null))
            assertEquals("vi", prefs.getString("app_language", null))
            assertEquals(5, repository.getQuickCurrencies().size)
        } finally {
            val editor = prefs.edit().clear()
            previous.forEach { (key, value) ->
                when (value) {
                    is String -> editor.putString(key, value)
                    is Boolean -> editor.putBoolean(key, value)
                    is Int -> editor.putInt(key, value)
                    is Long -> editor.putLong(key, value)
                    is Float -> editor.putFloat(key, value)
                    is Set<*> -> editor.putStringSet(key, value.filterIsInstance<String>().toSet())
                }
            }
            editor.commit()
        }
    }
}
