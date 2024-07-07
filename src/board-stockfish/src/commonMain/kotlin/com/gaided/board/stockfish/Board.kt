@file:Suppress("RedundantVisibilityModifier")

package com.gaided.board.stockfish

import com.gaided.model.FenNotation
import com.gaided.model.MoveNotation
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

public class Board internal constructor(
    private val api: RemoteBoardApi,
    private val ioContext: CoroutineContext = Dispatchers.IO
) {

    public constructor(
        url: String,
        ioContext: CoroutineContext = Dispatchers.IO
    ) : this(
        api = RemoteBoardApi(url = url),
        ioContext = ioContext
    )

    private val gson = Gson()

    public suspend fun getPosition(): FenNotation = withContext(ioContext) {
        val position = api.getFenPosition()
        FenNotation.fromFenString(position)
    }

    public suspend fun move(position: FenNotation, move: MoveNotation): Unit = withContext(ioContext) {
        api.makeMoves(position.fenString, listOf(move))
    }

    public suspend fun setPosition(position: FenNotation): Unit = withContext(ioContext) {
        api.setFenPosition(position.fenString)
    }

    public suspend fun getEvaluation(position: FenNotation): Evaluation = withContext(ioContext) {
        val evaluation = api.getEvaluation(position.fenString)
        gson.fromJson(evaluation, Evaluation::class.java)
    }

    public suspend fun isMoveCorrect(position: FenNotation, move: MoveNotation): Boolean = withContext(ioContext) {
        api.isMoveCorrect(position.fenString, move)
    }

    public data class Evaluation(
        public val type: String,
        public val value: Int
    )
}