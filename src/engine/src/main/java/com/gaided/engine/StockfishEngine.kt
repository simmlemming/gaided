package com.gaided.engine

import com.gaided.engine.Engine.TopMove
import com.gaided.engine.api.StockfishEngineApi
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

public class StockfishEngine(
    private val api: StockfishEngineApi,
    private val logger: Logger = DefaultLogger,
    private val ioContext: CoroutineContext = Dispatchers.IO,
) : Engine {

    public constructor(
        url: String,
        logger: Logger = DefaultLogger,
        ioContext: CoroutineContext = Dispatchers.IO,
    ) : this(
        api = StockfishEngineApi(url = url, logger = logger),
        logger = logger,
        ioContext = ioContext
    )

    public companion object {
        public const val NAME: String = "Stockfish 15"
    }

    override val name: String = NAME
    override val recommendedNumberOfMoves: Int = 3

    private val gson = Gson()

    override suspend fun getTopMoves(position: FenNotation, numberOfMoves: Int): List<TopMove> =
        withContext(ioContext) {
            val moves = api.getTopMoves(position.fenString, numberOfMoves)

            val type = object : TypeToken<List<StockfishApiTopMove>>() {}.type
            val topMoves = gson
                .fromJson<List<StockfishApiTopMove>>(moves, type)
                .map { TopMove(name, it.move, it.centipawn) }

            logger.i("$name: ${topMoves.map { it.move }}")

            topMoves
        }
}

private data class StockfishApiTopMove(
    @SerializedName("Move")
    val move: String,
    @SerializedName("Centipawn")
    val centipawn: Int
)
