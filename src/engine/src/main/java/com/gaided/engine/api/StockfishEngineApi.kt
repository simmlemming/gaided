package com.gaided.engine.api

import com.gaided.engine.DefaultLogger
import com.gaided.engine.Logger
import java.net.HttpURLConnection
import java.net.URL

public class StockfishEngineApi(
    url: String,
    openConnection: ((URL) -> HttpURLConnection) = { it.openConnection() as HttpURLConnection },
    logger: Logger = DefaultLogger
) : StockfishApi(url, openConnection, logger) {

    public suspend fun getTopMoves(position: String, numberOfMoves: Int): String = withPosition(position) {
        call("get_top_moves", numberOfMoves)
    }
}