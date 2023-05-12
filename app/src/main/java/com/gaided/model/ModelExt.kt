package com.gaided.model

import com.gaided.Game
import com.gaided.domain.Engine
import com.gaided.domain.MoveNotation
import kotlinx.coroutines.flow.MutableStateFlow

internal val Game.State.playerToMove: Game.Player
    get() {
        currentMove ?: return Game.Player.None

        if (currentMove.whiteMove == null) {
            return Game.Player.White
        }

        if (currentMove.blackMove == null) {
            return Game.Player.Black
        }

        return Game.Player.None
    }

internal val Game.State.isWaitingForTopMoves: Boolean
    get() {
        currentMove ?: return false

        if (playerToMove == Game.Player.White && currentMove.whiteTopMoves == null) {
            return true
        }

        if (playerToMove == Game.Player.Black && currentMove.blackTopMoves == null) {
            return true
        }

        return false
    }

internal fun Game.State.getTopMovesOfPlayerToMove(): List<Engine.TopMove>? {
    return when(playerToMove) {
        Game.Player.White -> currentMove?.whiteTopMoves
        Game.Player.Black -> currentMove?.blackTopMoves
        Game.Player.None -> null
    }
}

internal fun MutableStateFlow<Game.State>.setTopMoves(
    player: Game.Player, topMoves: List<Engine.TopMove>
) = updateCurrentMove { currentMove ->
    when (player) {
        Game.Player.White -> currentMove.copy(whiteTopMoves = topMoves)
        Game.Player.Black -> currentMove.copy(blackTopMoves = topMoves)
        else -> throw IllegalArgumentException("Unexpected player, player = $player")
    }
}

internal fun MutableStateFlow<Game.State>.setMove(
    player: Game.Player, move: MoveNotation
) = updateCurrentMove { currentMove ->
    when (player) {
        Game.Player.White -> currentMove.copy(whiteMove = move)
        Game.Player.Black -> currentMove.copy(blackMove = move)
        else -> throw IllegalArgumentException("Unexpected player, player = $player")
    }
}

private fun MutableStateFlow<Game.State>.updateCurrentMove(update: (Game.PartialHistoryRecord) -> Game.PartialHistoryRecord) {
    value = value.copy(currentMove = update(value.currentMove!!))

}

internal fun Game.PartialHistoryRecord.toHistoryRecord() = Game.HistoryRecord(
    whiteMove = this.whiteMove!!,
    whiteTopMoves = this.whiteTopMoves.orEmpty(),
    statsAfterWhiteMove = Game.Stats(0, 0, 0),
    blackMove = this.blackMove!!,
    blackTopMoves = this.blackTopMoves.orEmpty(),
    statsAfterBlackMove = Game.Stats(0, 0, 0),
)