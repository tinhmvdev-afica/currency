package com.example.currency.data.local

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class RefreshInfo : RealmObject {

    @PrimaryKey
    var key: String = ""

    var lastUpdated: Long = 0L
}