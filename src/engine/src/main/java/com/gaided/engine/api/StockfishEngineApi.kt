package com.gaided.engine.api

import com.gaided.network.StockfishApi
import java.net.HttpURLConnection
import java.net.URL

internal class StockfishEngineApi(
    url: String,
    openConnection: ((URL) -> HttpURLConnection) = { it.openConnection() as HttpURLConnection },
) : StockfishApi(url, openConnection) {

    suspend fun getTopMoves(position: String, numberOfMoves: Int): String = withPosition(position) {
        call("get_top_moves", numberOfMoves)
    }
}