package com.example.currency.presentation.common

import android.content.Context
import com.example.currency.R

object UpdatedAtFormatter {
    fun format(context: Context, updatedAt: Long): String {
        val elapsedMinutes = ((System.currentTimeMillis() - updatedAt) / 60_000).coerceAtLeast(0)
        return when {
            elapsedMinutes < 1 -> context.getString(R.string.updated_just_now)
            elapsedMinutes < 60 -> context.getString(R.string.updated_minutes_ago, elapsedMinutes)
            elapsedMinutes < 120 -> context.getString(R.string.updated_hour_ago)
            elapsedMinutes < 24 * 60 -> context.getString(R.string.updated_hours_ago, elapsedMinutes / 60)
            elapsedMinutes < 48 * 60 -> context.getString(R.string.updated_yesterday)
            else -> context.getString(R.string.updated_days_ago, elapsedMinutes / (24 * 60))
        }
    }
}
