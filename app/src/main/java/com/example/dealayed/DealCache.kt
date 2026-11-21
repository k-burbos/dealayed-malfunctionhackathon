package com.example.dealayed

import android.content.Context

object DealCache {
    private var cached: List<Deal>? = null

    fun get(context: Context): List<Deal> =
        cached ?: DealRepository.loadDeals(context.applicationContext).also { cached = it }
}