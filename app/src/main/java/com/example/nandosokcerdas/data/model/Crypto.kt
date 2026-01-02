package com.example.nandosokcerdas.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Crypto(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("symbol")
    val symbol: String,

    @SerializedName("current_price")
    val price: Double,

    @SerializedName("price_change_percentage_24h")
    val change24h: Double,

    @SerializedName("image")
    val imageUrl: String
) : Parcelable
