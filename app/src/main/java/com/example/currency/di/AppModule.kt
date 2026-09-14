package com.example.currency.di

import com.example.currency.data.api.CoinGeckoApi
import com.example.currency.data.api.CurrencyFreaksApi
import com.example.currency.data.api.RetrofitClient
import com.example.currency.data.local.RealmDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.realm.kotlin.Realm
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideCoinGeckoApi(): CoinGeckoApi = RetrofitClient.api
    @Provides
    @Singleton
    fun provideCurrencyFreaksApi(): CurrencyFreaksApi = RetrofitClient.currencyFreaksApi

    @Provides
    @Singleton
    fun provideRealm(): Realm = RealmDatabase.realm
}