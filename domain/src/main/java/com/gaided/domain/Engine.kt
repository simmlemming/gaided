@file:Suppress("RedundantVisibilityModifier")

package com.gaided.domain

import com.gaided.domain.api.StockfishApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

public class Engine(
    private val api: StockfishApi,
    private val ioContext: CoroutineContext = Dispatchers.IO
) {

    public suspend fun getFenPosition(): String = withContext(ioContext) {
        val response = api.getFenPosition()
        response
    }
}