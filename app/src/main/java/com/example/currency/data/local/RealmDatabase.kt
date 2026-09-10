package com.example.currency.data.local

import com.example.currency.data.local.CurrencyRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration

object RealmDatabase {

    private val config = RealmConfiguration.Builder(
        schema = setOf(
            CurrencyRealm::class,
            RefreshInfo::class
        )
    )
        .name("currency.realm")
        .build()

    val realm: Realm by lazy {
        Realm.open(config)
    }
}