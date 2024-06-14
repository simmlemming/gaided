@file:Suppress("RedundantVisibilityModifier")

package com.gaided.engine

import com.gaided.engine.api.StockfishApi
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

public class RemoteBoard(
    private val api: StockfishApi,
    private val ioContext: CoroutineContext = Dispatchers.IO
) {

    private val gson = Gson()

    public suspend fun getFenPosition(): String = withContext(ioContext) {
        val response = api.getFenPosition()
        response
    }

    public suspend fun getTopMoves(position: FenNotation, numberOfMoves: Int): List<TopMove> = withContext(ioContext) {
        val moves = api.getTopMoves(position.fenString, numberOfMoves)

        val type = object : TypeToken<List<TopMove>>() {}.type
        gson.fromJson(moves, type)
    }

    public suspend fun move(position: FenNotation, move: MoveNotation): Unit = withContext(ioContext) {
        api.makeMoves(position.fenString, listOf(move))
    }

    public suspend fun setFenPosition(position: FenNotation): Unit = withContext(ioContext) {
        api.setFenPosition(position.fenString)
    }

    public suspend fun getEvaluation(position: FenNotation): Evaluation = withContext(ioContext) {
        val evaluation = api.getEvaluation(position.fenString)
        gson.fromJson(evaluation, Evaluation::class.java)
    }

    public suspend fun isMoveCorrect(position: FenNotation, move: MoveNotation): Boolean = withContext(ioContext) {
        api.isMoveCorrect(position.fenString, move)
    }

    public data class TopMove(
        @SerializedName("Move")
        public val move: String,
        @SerializedName("Centipawn")
        public val centipawn: Int
    )

    public data class Evaluation(
        public val type: String,
        public val value: Int
    )
}