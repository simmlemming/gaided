package com.gaided

import com.gaided.api.StockfishApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

internal class Engine(
    private val api: StockfishApi,
    private val ioContext: CoroutineContext = Dispatchers.IO
) {

    suspend fun getFenPosition(): String = withContext(ioContext) {
        val response = api.getFenPosition()
        response
    }
}