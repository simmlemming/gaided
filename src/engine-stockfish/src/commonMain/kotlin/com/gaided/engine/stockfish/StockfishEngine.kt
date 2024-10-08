package com.gaided.engine.stockfish

import com.gaided.engine.Engine
import com.gaided.engine.Engine.TopMove
import com.gaided.logger.Logger
import com.gaided.model.FenNotation
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

public fun createStockfishEngine(
    url: String,
    ioContext: CoroutineContext = Dispatchers.IO,
): Engine = StockfishEngine(
    api = StockfishEngineApi(url = url),
    ioContext = ioContext
)

public const val STOCKFISH_ENGINE_NAME: String = "Stockfish 15"

internal class StockfishEngine internal constructor(
    private val api: StockfishEngineApi,
    private val ioContext: CoroutineContext = Dispatchers.IO,
) : Engine {

    override val name: String = STOCKFISH_ENGINE_NAME
    override val recommendedNumberOfMoves: Int = 3
    private val gson = Gson()

    override suspend fun getTopMoves(position: FenNotation, numberOfMoves: Int): List<TopMove> =
        withContext(ioContext) {
            val moves = api.getTopMoves(position.fenString, numberOfMoves)

            val type = object : TypeToken<List<StockfishApiTopMove>>() {}.type
            val topMoves = gson
                .fromJson<List<StockfishApiTopMove>>(moves, type)
                .map { TopMove(name, it.move, it.centipawn) }

            Logger.i("$name: ${topMoves.map { it.move }}")

            topMoves
        }
}

private data class StockfishApiTopMove(
    @SerializedName("Move")
    val move: String,
    @SerializedName("Centipawn")
    val centipawn: Int
)
