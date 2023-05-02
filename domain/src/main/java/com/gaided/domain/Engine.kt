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

    public suspend fun getTopMoves(numberOfMoves: Int): String = withContext(ioContext) {
        val moves = api.getTopMoves(numberOfMoves)
        println(moves)
        moves
    }

    public suspend fun makeMoveFromCurrentPosition(from: SquareNotation, to: SquareNotation): Unit =
        withContext(ioContext) {
            val moves = listOf("$from$to")
            api.makeMovesFromCurrentPosition(moves)
        }

    public suspend fun setFenPosition(position: FenString): Unit = withContext(ioContext) {
        api.setFenPosition(position)
    }
}