package com.example.currency.data.local


import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class CurrencyRealm : RealmObject {

    @PrimaryKey
    var id: String = ""

    var symbol: String = ""

    var name: String = ""

    var isCrypto: Boolean = false

    var iconUrl: String? = null

    var symbolChar: String = ""

    var priceInUsd: Double = 0.0

    var priceChange24h: Double? = null
    var marketCap: Double? = null
    var marketCapRank: Int? = null
}