package com.example.currency.data.local.realm

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.EmbeddedRealmObject
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class ChartCacheRealm : RealmObject {

    @PrimaryKey
    var key: String = ""

    var coinId: String = ""
    var days: Int = 1
    var updatedAt: Long = 0L
    var points: RealmList<PricePointRealm> = realmListOf()
}

class PricePointRealm : EmbeddedRealmObject {
    var timestamp: Long = 0L
    var price: Double = 0.0
}
