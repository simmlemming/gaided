@file:Suppress("RedundantVisibilityModifier")

package com.gaided.engine.api

import com.gaided.engine.MoveNotation
import kotlinx.coroutines.sync.withLock
import java.net.HttpURLConnection
import java.net.URL

public open class RemoteBoardApi(
    baseUrl: String,
    openConnection: ((URL) -> HttpURLConnection) = { url -> url.openConnection() as HttpURLConnection }
) : StockfishApi(baseUrl, openConnection) {

    public suspend fun getFenPosition(): String = mutex.withLock {
        call("get_fen_position")
    }

    public suspend fun setFenPosition(position: String): Unit = withPosition(position) {

    }

    public suspend fun makeMoves(position: String, moves: List<String>): Unit = withPosition(position) {
        call("make_moves_from_current_position", moves)
    }

    public suspend fun getEvaluation(position: String): String = withPosition(position) {
        call("get_evaluation")
    }

    public suspend fun isMoveCorrect(position: String, move: MoveNotation): Boolean = withPosition(position) {
        val response = call("is_move_correct", move)
        response == "True"
    }
}