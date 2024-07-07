@file:Suppress("RedundantVisibilityModifier")

package com.gaided.stockfish.board

import com.gaided.model.MoveNotation
import com.gaided.network.StockfishApi
import kotlinx.coroutines.sync.withLock
import java.net.HttpURLConnection
import java.net.URL

internal open class RemoteBoardApi(
    url: String,
    openConnection: ((URL) -> HttpURLConnection) = { it.openConnection() as HttpURLConnection },
) : StockfishApi(url, openConnection) {

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
