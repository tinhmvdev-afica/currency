package com.example.currency.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MarketChartDto(@SerializedName("prices") val prices: List<List<Double>>)
