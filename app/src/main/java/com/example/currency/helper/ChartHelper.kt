package com.example.currency.helper

import com.example.currency.data.model.PricePoint
import kotlin.collections.mapIndexed
import com.github.mikephil.charting.data.Entry
import java.text.SimpleDateFormat
import java.util.Locale

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
}