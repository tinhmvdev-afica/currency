package com.example.currency.data.local

import com.example.currency.data.local.CurrencyRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.migration.AutomaticSchemaMigration

object RealmDatabase {

    private val config = RealmConfiguration.Builder(
        schema = setOf(
            CurrencyRealm::class,
            RefreshInfo::class
        )
    )
        .name("currency.realm")
        .schemaVersion(1)
        .migration(AutomaticSchemaMigration {})
        .build()

    val realm: Realm by lazy {
        Realm.open(config)
    }
}
