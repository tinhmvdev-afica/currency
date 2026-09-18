package com.example.currency.presentation.common.format

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

object CurrencyFormatHelper {

    private const val compactThreshold = 100_000_000

    private val usSymbols = DecimalFormatSymbols(Locale.US).apply {
        groupingSeparator = ','
        decimalSeparator = '.'
    }

    fun formatNumber(value: Double): String = when {
        value >= 1_000_000 -> DecimalFormat("#,###", usSymbols).format(value)
        value >= 1 -> DecimalFormat("#,##0.00", usSymbols).format(value)
        value >= 0.0001 -> DecimalFormat("#,##0.0000", usSymbols).format(value)
        value > 0 -> DecimalFormat("0.00000000", usSymbols).format(value)
        else -> "0.00"
    }

    /**
     * Formats large converter results compactly so the output remains on one line.
     * The value has no currency symbol because the converter displays it separately.
     */
    fun formatOutputAmount(value: Double): String {
        if (abs(value) < compactThreshold) return formatNumber(value)

        val (scaledValue, suffix) = when {
            abs(value) >= 1_000_000_000_000 -> value / 1_000_000_000_000 to "T"
            abs(value) >= 1_000_000_000 -> value / 1_000_000_000 to "B"
            else -> value / 1_000_000 to "M"
        }
        return DecimalFormat("#,##0.##", usSymbols).format(scaledValue) + suffix
    }

    /** Formats an editable amount while preserving its decimal part, including a trailing dot. */
    fun formatInputAmount(value: String): String {
        if (value.isEmpty()) return value

        val ungroupedValue = value.replace(",", "")
        val decimalIndex = ungroupedValue.indexOf('.')
        val integerPart = if (decimalIndex >= 0) {
            ungroupedValue.substring(0, decimalIndex)
        } else {
            ungroupedValue
        }
        val decimalPart = if (decimalIndex >= 0) ungroupedValue.substring(decimalIndex) else ""
        val normalizedIntegerPart = integerPart.trimStart('0').ifEmpty { "0" }
        val groupedIntegerPart = normalizedIntegerPart.reversed()
            .chunked(3)
            .joinToString(",")
            .reversed()

        return groupedIntegerPart + decimalPart
    }

    fun formatPercentage(value: Double): String = String.format(Locale.US, "%.2f", value)
}
