package com.gaided.engine

import com.gaided.engine.RemoteBoard.TopMove
import com.gaided.engine.api.RemoteBoardApi
import com.gaided.engine.api.StockfishEngineApi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

public class StockfishEngine(
    private val stockfishApi: StockfishEngineApi,
    private val ioContext: CoroutineContext = Dispatchers.IO
) : Engine {
    override val name: String = "Stockfish"

    private val gson = Gson()

    override suspend fun getTopMoves(position: FenNotation, numberOfMoves: Int): List<TopMove> =
        withContext(ioContext) {
            val moves = stockfishApi.getTopMoves(position.fenString, numberOfMoves)

            val type = object : TypeToken<List<TopMove>>() {}.type
            gson.fromJson(moves, type)
        }
}