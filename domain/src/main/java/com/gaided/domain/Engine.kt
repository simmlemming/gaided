@file:Suppress("RedundantVisibilityModifier")

package com.gaided.domain

import com.gaided.domain.api.StockfishApi
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

public class Engine(
    private val api: StockfishApi,
    private val ioContext: CoroutineContext = Dispatchers.IO
) {

    private val gson = Gson()

    public suspend fun getFenPosition(): String = withContext(ioContext) {
        val response = api.getFenPosition()
        response
    }

    public suspend fun getTopMoves(position: FenNotation, numberOfMoves: Int): List<TopMove> = withContext(ioContext) {
        api.setFenPosition(position.fenString)
        val moves = api.getTopMoves(numberOfMoves)
        println(moves)

        val type = object : TypeToken<List<TopMove>>() {}.type
        gson.fromJson(moves, type)
    }

    public suspend fun move(position: FenNotation, move: MoveNotation): Unit = withContext(ioContext) {
        api.setFenPosition(position.fenString)
        val moves = listOf(move)
        api.makeMovesFromCurrentPosition(moves)
    }

    public suspend fun makeMoveFromCurrentPosition(from: SquareNotation, to: SquareNotation): Unit =
        withContext(ioContext) {
            val moves = listOf("$from$to")
            api.makeMovesFromCurrentPosition(moves)
        }

    public suspend fun setFenPosition(position: FenNotation): Unit = withContext(ioContext) {
        api.setFenPosition(position.fenString)
    }

    public data class TopMove(
        @SerializedName("Move")
        public val move: String,
        @SerializedName("Centipawn")
        public val centipawn: Int
    )
}