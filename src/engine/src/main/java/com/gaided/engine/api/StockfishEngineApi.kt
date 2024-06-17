package com.gaided.engine.api

import com.gaided.engine.DefaultLogger
import com.gaided.engine.Logger
import java.net.HttpURLConnection
import java.net.URL

public class StockfishEngineApi(
    baseUrl: String,
    openConnection: ((URL) -> HttpURLConnection) = { url -> url.openConnection() as HttpURLConnection },
    logger: Logger = DefaultLogger
) : StockfishApi(baseUrl, openConnection, logger) {

    public suspend fun getTopMoves(position: String, numberOfMoves: Int): String = withPosition(position) {
        call("get_top_moves", numberOfMoves)
    }
}