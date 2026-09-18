package com.example.currency.di

import com.example.currency.data.local.realm.ChartCacheRealm
import com.example.currency.data.local.realm.CurrencyRealm
import com.example.currency.data.local.realm.PricePointRealm
import com.example.currency.data.local.realm.RefreshInfo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.migration.AutomaticSchemaMigration
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideRealm(): Realm = Realm.open(
        RealmConfiguration.Builder(
            setOf(
                CurrencyRealm::class,
                RefreshInfo::class,
                ChartCacheRealm::class,
                PricePointRealm::class
            )
        )
            .name("currency.realm")
            .schemaVersion(1)
            .migration(AutomaticSchemaMigration {})
            .build()
    )
}
