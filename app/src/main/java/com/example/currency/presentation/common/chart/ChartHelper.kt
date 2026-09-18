package com.example.currency.presentation.common.chart

import com.example.currency.domain.model.PricePoint
import kotlin.collections.mapIndexed
import com.github.mikephil.charting.data.Entry
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs

object ChartHelper {

    fun reducePoints(
        pricePoints: List<PricePoint>,
        maxPoints: Int = 80
    ): List<PricePoint> {

        if (pricePoints.size <= maxPoints) {
            return pricePoints
        }

        val step = pricePoints.size.toDouble() / maxPoints

        return List(maxPoints) { index ->
            pricePoints[(index * step).toInt()]
        }
    }

    fun createEntries(
        pricePoints: List<PricePoint>
    ): List<Entry> {

        return pricePoints.mapIndexed { index, point ->
            Entry(
                index.toFloat(),
                point.price.toFloat()
            )
        }
    }

    fun getTimeFormatter(days: Int): SimpleDateFormat {
        val pattern = when (days) {
            1 -> "HH:mm"
            7, 30, 90 -> "dd/MM"
            365 -> "MM/yyyy"
            else -> "dd/MM"
        }

        return SimpleDateFormat(
            pattern,
            Locale.getDefault()
        )
    }

    fun formatPrice(value: Float): String {
        return when {
            value >= 1000 ->
                String.format(Locale.US, "$%,.0f", value)

            value >= 1 ->
                String.format(Locale.US, "$%,.2f", value)

            value >= 0.01 ->
                String.format(Locale.US, "$%.4f", value)

            value >= 0.0001 ->
                String.format(Locale.US, "$%.6f", value)

            else ->
                String.format(Locale.US, "$%.8f", value)
        }
    }

    fun formatCompactCurrency(value: Double?): String {
        if (value == null) return "—"

        val absoluteValue = abs(value)
        val (scaledValue, suffix) = when {
            absoluteValue >= 1_000_000_000_000 -> value / 1_000_000_000_000 to "T"
            absoluteValue >= 1_000_000_000 -> value / 1_000_000_000 to "B"
            absoluteValue >= 1_000_000 -> value / 1_000_000 to "M"
            absoluteValue >= 1_000 -> value / 1_000 to "K"
            else -> value to ""
        }
        val formatter = DecimalFormat(
            "#,##0.##",
            DecimalFormatSymbols(Locale.US)
        )
        return "$${formatter.format(scaledValue)}$suffix"
    }
}
