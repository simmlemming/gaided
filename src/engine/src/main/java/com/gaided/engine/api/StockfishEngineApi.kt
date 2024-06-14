package com.gaided.engine.api

import java.net.HttpURLConnection
import java.net.URL

public class StockfishEngineApi(
    baseUrl: String,
    openConnection: ((URL) -> HttpURLConnection) = { url -> url.openConnection() as HttpURLConnection }
) : StockfishApi(baseUrl, openConnection) {

    public suspend fun getTopMoves(position: String, numberOfMoves: Int): String = withPosition(position) {
        call("get_top_moves", numberOfMoves)
    }
}