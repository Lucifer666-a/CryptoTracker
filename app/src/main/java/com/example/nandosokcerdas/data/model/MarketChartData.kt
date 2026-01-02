package com.example.nandosokcerdas.data.model

import com.google.gson.annotations.SerializedName

data class MarketChartData(
    @SerializedName("prices")
    val prices: List<List<Double>>
)
