package com.example.currency.di

import com.example.currency.data.local.preferences.UserPreferencesRepositoryImpl
import com.example.currency.data.repository.CurrencyRepositoryImpl
import com.example.currency.domain.repository.CurrencyRepository
import com.example.currency.domain.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindCurrencyRepository(impl: CurrencyRepositoryImpl): CurrencyRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(impl: UserPreferencesRepositoryImpl): UserPreferencesRepository
}
