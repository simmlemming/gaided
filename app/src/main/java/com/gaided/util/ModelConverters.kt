package com.gaided.util

import android.graphics.Color
import com.gaided.Game
import com.gaided.domain.Board
import com.gaided.domain.Engine
import com.gaided.domain.SquareNotation
import com.gaided.view.chessboard.ChessBoardView
import com.gaided.view.player.PlayerView

internal fun Game.State.toPlayerViewState(player: Game.Player) = when (this) {
    is Game.State.Initialized -> PlayerView.State.EMPTY
    is Game.State.WaitingForMove -> {
        if (this.player == player) {
            this.toPlayerViewState()
        } else {
            PlayerView.State.OPPONENT_MOVE
        }
    }
}

private fun Game.State.WaitingForMove.toPlayerViewState(): PlayerView.State {
    val moves = this.topMoves.shuffled()
    return PlayerView.State(
        progressVisible = this.waitingForTopMoves,
        move1 = moves.getOrNull(0).toPlayerViewMoveState(),
        move2 = moves.getOrNull(1).toPlayerViewMoveState(),
        move3 = moves.getOrNull(2).toPlayerViewMoveState()
    )
}

private fun Engine.TopMove?.toPlayerViewMoveState() = PlayerView.State.Move(
    this?.move ?: "",
    isVisible = this != null,
    text = this?.move ?: ""
)

internal fun Board.Arrow.toArrowViewState() = ChessBoardView.State.Arrow(
    this.start,
    this.end,
    Color.parseColor("#648EBA")
)

internal fun Map.Entry<SquareNotation, Board.Piece>.toPieceViewState() = ChessBoardView.State.Piece(
    this.key,
    if (this.value.isBlack) Color.BLACK else Color.WHITE
)