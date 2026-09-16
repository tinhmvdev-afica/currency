package com.example.currency

import com.example.currency.helper.ChartHelper
import com.example.currency.helper.CurrencyFormatHelper
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun formatCompactCurrency_usesReadableSuffixes() {
        assertEquals("$1.52T", ChartHelper.formatCompactCurrency(1_521_304_491_952.0))
        assertEquals("$39.84B", ChartHelper.formatCompactCurrency(39_839_006_172.0))
        assertEquals("$725M", ChartHelper.formatCompactCurrency(725_000_000.0))
        assertEquals("$850K", ChartHelper.formatCompactCurrency(850_000.0))
        assertEquals("—", ChartHelper.formatCompactCurrency(null))
    }

    @Test
    fun formatNumber_usesConverterDisplayRules() {
        assertEquals("1,000,000", CurrencyFormatHelper.formatNumber(1_000_000.0))
        assertEquals("12.35", CurrencyFormatHelper.formatNumber(12.345))
        assertEquals("0.0012", CurrencyFormatHelper.formatNumber(0.001234))
        assertEquals("0.00001234", CurrencyFormatHelper.formatNumber(0.00001234))
        assertEquals("0.00", CurrencyFormatHelper.formatNumber(0.0))
    }

    @Test
    fun formatOutputAmount_compactsOnlyLargeConverterResults() {
        assertEquals("2,650,000", CurrencyFormatHelper.formatOutputAmount(2_650_000.0))
        assertEquals("265M", CurrencyFormatHelper.formatOutputAmount(265_000_000.0))
        assertEquals("2.65B", CurrencyFormatHelper.formatOutputAmount(2_650_000_000.0))
        assertEquals("1.52T", CurrencyFormatHelper.formatOutputAmount(1_521_304_491_952.0))
    }

    @Test
    fun formatInputAmount_addsThousandsSeparatorsAndKeepsDecimals() {
        assertEquals("1,000,000", CurrencyFormatHelper.formatInputAmount("1000000"))
        assertEquals("1,000.5", CurrencyFormatHelper.formatInputAmount("1000.5"))
        assertEquals("1,000.", CurrencyFormatHelper.formatInputAmount("1000."))
        assertEquals("0.5", CurrencyFormatHelper.formatInputAmount("0000.5"))
    }
}
