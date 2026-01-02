package com.example.nandosokcerdas.data.remote

import com.example.nandosokcerdas.data.model.Crypto
import com.example.nandosokcerdas.data.model.MarketChartData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CryptoApi {

    @GET("coins/markets")
    suspend fun getMarketData(
        @Query("vs_currency") vsCurrency: String = "usd"
    ): Response<List<Crypto>>

    @GET("coins/{id}/market_chart")
    suspend fun getMarketChart(
        @Path("id") id: String,
        @Query("vs_currency") vsCurrency: String = "usd",
        @Query("days") days: String = "7"
    ): Response<MarketChartData>
}
