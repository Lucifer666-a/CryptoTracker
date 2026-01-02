package com.example.nandosokcerdas.data.remote

class CryptoRepository {

    private val api = RetrofitInstance.api

    suspend fun getCryptoList() = api.getMarketData()

    suspend fun getMarketChart(id: String) = api.getMarketChart(id)
}
