package com.example.currency.data.repository

import com.example.currency.data.local.realm.ChartCacheRealm
import com.example.currency.data.local.realm.CurrencyRealm
import com.example.currency.data.local.realm.PricePointRealm
import com.example.currency.data.local.realm.RefreshInfo
import com.example.currency.data.local.asset.AssetCurrencySeedDataSource
import com.example.currency.data.mapper.toCoinMarketItem
import com.example.currency.data.mapper.toCurrencyItem
import com.example.currency.data.mapper.toFiatCurrencyItems
import com.example.currency.data.mapper.toRealm
import com.example.currency.data.remote.api.CoinGeckoApi
import com.example.currency.data.remote.api.CurrencyFreaksApi
import com.example.currency.data.network.NetworkMonitor
import com.example.currency.domain.model.ChartLoadResult
import com.example.currency.domain.model.CoinMarketItem
import com.example.currency.domain.model.CurrencyListLoadResult
import com.example.currency.domain.model.CurrencyItem
import com.example.currency.domain.model.PricePoint
import com.example.currency.domain.repository.CurrencyRepository
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

class CurrencyRepositoryImpl @Inject constructor(
    private val coinGeckoApi: CoinGeckoApi,
    private val currencyFreaksApi: CurrencyFreaksApi,
    private val realm: Realm,
    private val networkMonitor: NetworkMonitor,
    private val assetCurrencySeedDataSource: AssetCurrencySeedDataSource
) : CurrencyRepository {
    private val seedMutex = Mutex()

    private companion object {
        const val CRYPTO_KEY = "crypto"
        const val FIAT_KEY = "fiat"
        const val CRYPTO_REFRESH_TIME = 5 * 60 * 1000L
        const val FIAT_REFRESH_TIME = 30 * 60 * 1000L
        const val MAX_CACHED_CHART_POINTS = 80
    }

    override fun getCoinMarket(forceRefresh: Boolean): Flow<CurrencyListLoadResult<CoinMarketItem>> = flow {
        ensureSeeded()
        val cached = getCachedCoinMarkets()
        val updatedAt = getRefreshTime(CRYPTO_KEY)
        val needsRefresh = forceRefresh || cached.isEmpty() || shouldRefresh(CRYPTO_KEY, CRYPTO_REFRESH_TIME)
        if (!needsRefresh) {
            emit(CurrencyListLoadResult.Data(items = cached, updatedAt = updatedAt, isCached = true))
            return@flow
        }

        if (!networkMonitor.hasInternetConnection()) {
            emit(
                if (cached.isEmpty()) CurrencyListLoadResult.OfflineNoCache
                else CurrencyListLoadResult.Data(items = cached, updatedAt = updatedAt, isCached = true, isOffline = true)
            )
            return@flow
        }

        if (cached.isNotEmpty()) {
            emit(CurrencyListLoadResult.Data(items = cached, updatedAt = updatedAt, isCached = true, isRefreshing = true))
        }

        try {
            emit(
                CurrencyListLoadResult.Data(
                    items = refreshCoinMarket().getOrThrow(),
                    updatedAt = getRefreshTime(CRYPTO_KEY),
                    isCached = false
                )
            )
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            emit(
                if (cached.isEmpty()) CurrencyListLoadResult.NetworkError
                else CurrencyListLoadResult.Data(items = cached, updatedAt = updatedAt, isCached = true, refreshFailed = true)
            )
        }
    }

    override fun getFiats(forceRefresh: Boolean): Flow<CurrencyListLoadResult<CurrencyItem>> = flow {
        ensureSeeded()
        val cached = getCachedFiats()
        val updatedAt = getRefreshTime(FIAT_KEY)
        val needsRefresh = forceRefresh || cached.isEmpty() || shouldRefresh(FIAT_KEY, FIAT_REFRESH_TIME)
        if (!needsRefresh) {
            emit(CurrencyListLoadResult.Data(items = cached, updatedAt = updatedAt, isCached = true))
            return@flow
        }

        if (!networkMonitor.hasInternetConnection()) {
            emit(
                if (cached.isEmpty()) CurrencyListLoadResult.OfflineNoCache
                else CurrencyListLoadResult.Data(items = cached, updatedAt = updatedAt, isCached = true, isOffline = true)
            )
            return@flow
        }

        if (cached.isNotEmpty()) {
            emit(CurrencyListLoadResult.Data(items = cached, updatedAt = updatedAt, isCached = true, isRefreshing = true))
        }

        try {
            emit(
                CurrencyListLoadResult.Data(
                    items = refreshFiats().getOrThrow(),
                    updatedAt = getRefreshTime(FIAT_KEY),
                    isCached = false
                )
            )
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            emit(
                if (cached.isEmpty()) CurrencyListLoadResult.NetworkError
                else CurrencyListLoadResult.Data(items = cached, updatedAt = updatedAt, isCached = true, refreshFailed = true)
            )
        }
    }

    override suspend fun refreshCoinMarket(): Result<List<CoinMarketItem>> = runCatching {
        withContext(Dispatchers.IO) {
            val items = coinGeckoApi.getCoinMarkets().map { dto -> dto.toCoinMarketItem() }
            check(items.isNotEmpty()) { "Coin market response was empty" }
            realm.write {
                delete(query<CurrencyRealm>("isCrypto == true").find())
                items.forEach { coinItem ->
                    copyToRealm(coinItem.toRealm(), updatePolicy = UpdatePolicy.ALL)
                }
            }
            updateRefreshTime(CRYPTO_KEY)
            items
        }
    }.onFailure { error ->
        if (error is CancellationException) throw error
    }

    override suspend fun refreshFiats(): Result<List<CurrencyItem>> = runCatching {
        withContext(Dispatchers.IO) {
            val (supported, rates) = coroutineScope {
                val supportedRequest = async { currencyFreaksApi.getSupportedCurrencies() }
                val ratesRequest = async { currencyFreaksApi.getLatestRates() }
                supportedRequest.await() to ratesRequest.await()
            }
            val items = supported.toFiatCurrencyItems(rates)
            check(items.isNotEmpty()) { "Fiat currency response was empty" }
            realm.write {
                delete(query<CurrencyRealm>("isCrypto == false").find())
                items.forEach { currencyItem ->
                    copyToRealm(currencyItem.toRealm(), updatePolicy = UpdatePolicy.ALL)
                }
            }
            updateRefreshTime(FIAT_KEY)
            items
        }
    }.onFailure { error ->
        if (error is CancellationException) throw error
    }

    override fun getMarketChart(id: String, days: Int): Flow<ChartLoadResult> = flow {
        val cacheKey = "${id}_$days"
        val cached = realm.query<ChartCacheRealm>("key == $0", cacheKey).first().find()
                ?.let { chart ->
                    CachedChart(
                        points = chart.points.map { PricePoint(it.timestamp, it.price) },
                        updatedAt = chart.updatedAt
                    )
                }


        if (cached != null) {
            val isOnline = networkMonitor.hasInternetConnection()
            val shouldRefresh = isOnline && isChartExpired(cached.updatedAt, days)
            emit(
                ChartLoadResult.Data(
                    points = cached.points,
                    updatedAt = cached.updatedAt,
                    isCached = true,
                    isOffline = !isOnline,
                    isRefreshing = shouldRefresh
                )
            )
            if (!shouldRefresh) return@flow

            try {
                val freshPoints = fetchAndCacheChart(id, days, cacheKey)
                emit(
                    ChartLoadResult.Data(
                        points = freshPoints,
                        updatedAt = System.currentTimeMillis(),
                        isCached = false
                    )
                )
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                emit(
                    ChartLoadResult.Data(
                        points = cached.points,
                        updatedAt = cached.updatedAt,
                        isCached = true,
                        isOffline = !networkMonitor.hasInternetConnection(),
                        refreshFailed = true
                    )
                )
            }
            return@flow
        }

        if (!networkMonitor.hasInternetConnection()) {
            emit(ChartLoadResult.OfflineNoCache)
            return@flow
        }

        try {
            val points = fetchAndCacheChart(id, days, cacheKey)
            emit(
                ChartLoadResult.Data(
                    points = points,
                    updatedAt = System.currentTimeMillis(),
                    isCached = false
                )
            )
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            emit(
                if (networkMonitor.hasInternetConnection()) {
                    ChartLoadResult.NetworkError
                } else {
                    ChartLoadResult.OfflineNoCache
                }
            )
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun fetchAndCacheChart(id: String, days: Int, cacheKey: String): List<PricePoint> =
        withContext(Dispatchers.IO) {
            val points = downsample(
                coinGeckoApi.getCoinMarketChart(id, days).prices.map {
                    PricePoint(timestamp = it[0].toLong(), price = it[1])
                }
            )
            val updatedAt = System.currentTimeMillis()
            realm.write {
                val chart = query<ChartCacheRealm>("key == $0", cacheKey).first().find()
                    ?: copyToRealm(ChartCacheRealm().apply { key = cacheKey })
                chart.coinId = id
                chart.days = days
                chart.updatedAt = updatedAt
                chart.points.clear()
                points.forEach { point ->
                    chart.points.add(PricePointRealm().apply {
                        timestamp = point.timestamp
                        price = point.price
                    })
                }
            }
            points
        }

    private fun isChartExpired(updatedAt: Long, days: Int): Boolean =
        System.currentTimeMillis() - updatedAt >= chartTtl(days)

    private fun chartTtl(days: Int): Long = when (days) {
        1 -> 5 * 60 * 1000L
        7 -> 15 * 60 * 1000L
        30 -> 30 * 60 * 1000L
        90 -> 60 * 60 * 1000L
        365 -> 4 * 60 * 60 * 1000L
        else -> 30 * 60 * 1000L
    }

    private fun downsample(points: List<PricePoint>): List<PricePoint> {
        if (points.size <= MAX_CACHED_CHART_POINTS) return points
        return List(MAX_CACHED_CHART_POINTS) { index ->
            points[index * points.lastIndex / (MAX_CACHED_CHART_POINTS - 1)]
        }
    }

    private data class CachedChart(
        val points: List<PricePoint>,
        val updatedAt: Long
    )

    private suspend fun ensureSeeded() = seedMutex.withLock {
        val needsSeed = withContext(Dispatchers.IO) {
            realm.query<CurrencyRealm>().find().let { currencies ->
                currencies.none { it.isCrypto } || currencies.none { !it.isCrypto }
            }
        }
        if (!needsSeed) return@withLock

        val seed = assetCurrencySeedDataSource.load() ?: return@withLock
        withContext(Dispatchers.IO) {
            realm.write {
                val hasCrypto = query<CurrencyRealm>("isCrypto == true").find().isNotEmpty()
                if (!hasCrypto) {
                    seed.coinMarkets.forEach { coin ->
                        copyToRealm(coin.toRealm(), updatePolicy = UpdatePolicy.ALL)
                    }
                    updateRefreshInfo(CRYPTO_KEY, seed.cryptoUpdatedAt)
                }

                val hasFiats = query<CurrencyRealm>("isCrypto == false").find().isNotEmpty()
                if (!hasFiats) {
                    seed.fiats.forEach { fiat ->
                        copyToRealm(fiat.toRealm(), updatePolicy = UpdatePolicy.ALL)
                    }
                    updateRefreshInfo(FIAT_KEY, seed.fiatUpdatedAt)
                }
            }
        }
    }

    private fun io.realm.kotlin.MutableRealm.updateRefreshInfo(key: String, timestamp: Long) {
        val refreshInfo = query<RefreshInfo>("key == $0", key).first().find()
        if (refreshInfo == null) {
            copyToRealm(RefreshInfo().apply {
                this.key = key
                lastUpdated = timestamp
            })
        } else {
            refreshInfo.lastUpdated = timestamp
        }
    }

    private suspend fun getCachedCoinMarkets(): List<CoinMarketItem> = withContext(Dispatchers.IO) {
        realm.query<CurrencyRealm>("isCrypto == true").find()
            .map { realmItem -> realmItem.toCoinMarketItem() }
    }

    private suspend fun getCachedFiats(): List<CurrencyItem> = withContext(Dispatchers.IO) {
        realm.query<CurrencyRealm>("isCrypto == false").find()
            .map { realmItem -> realmItem.toCurrencyItem() }
    }

    private suspend fun getRefreshTime(key: String): Long? = withContext(Dispatchers.IO) {
        realm.query<RefreshInfo>("key == $0", key).first().find()?.lastUpdated
    }

    private suspend fun shouldRefresh(key: String, intervalMs: Long): Boolean = withContext(Dispatchers.IO) {
        val timestamp = realm.query<RefreshInfo>("key == $0", key).first().find()?.lastUpdated
            ?: return@withContext true
        System.currentTimeMillis() - timestamp >= intervalMs
    }

    private suspend fun updateRefreshTime(key: String) {
        realm.write {
            val refreshInfo = query<RefreshInfo>("key == $0", key).first().find()
            if (refreshInfo == null) {
                copyToRealm(RefreshInfo().apply {
                    this.key = key
                    lastUpdated = System.currentTimeMillis()
                })
            } else {
                refreshInfo.lastUpdated = System.currentTimeMillis()
            }
        }
    }
}
