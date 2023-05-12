package com.gaided.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import com.gaided.Game
import com.gaided.domain.Board
import com.gaided.domain.Engine
import com.gaided.domain.SquareNotation
import com.gaided.view.chessboard.ChessBoardView
import com.gaided.view.player.PlayerView

internal fun Game.State.toPlayerViewState(
    pieces: Map<SquareNotation, Board.Piece>,
    player: Game.Player
) = when (this) {
    is Game.State.Initialized -> PlayerView.State.EMPTY
    is Game.State.WaitingForMove -> {
        if (this.player == player) {
            this.toPlayerViewState(pieces)
        } else {
            PlayerView.State.OPPONENT_MOVE
        }
    }
}

private fun Game.State.WaitingForMove.toPlayerViewState(pieces: Map<SquareNotation, Board.Piece>): PlayerView.State {
    val moves = this.topMoves.shuffled()
    return PlayerView.State(
        progressVisible = this.waitingForTopMoves,
        move1 = moves.getOrNull(0).toPlayerViewMoveState(pieces),
        move2 = moves.getOrNull(1).toPlayerViewMoveState(pieces),
        move3 = moves.getOrNull(2).toPlayerViewMoveState(pieces)
    )
}

private fun Engine.TopMove?.toPlayerViewMoveState(pieces: Map<SquareNotation, Board.Piece>) = PlayerView.State.Move(
    this?.move ?: "",
    isVisible = this != null,
    text = this?.move ?: "",
    pieceDrawableName = pieces[this?.move?.take(2)]?.toDrawableName()
)

internal fun Board.Arrow.toArrowViewState() = ChessBoardView.State.Arrow(
    this.start,
    this.end,
    Color.parseColor("#648EBA")
)

internal fun Map.Entry<SquareNotation, Board.Piece>.toPieceViewState() = ChessBoardView.State.Piece(
    this.value.toDrawableName(),
    this.key,
    if (this.value.isBlack) Color.BLACK else Color.WHITE
)

private fun Board.Piece.toDrawableName(): String {
    val color = if (this.isBlack) "b" else "w"
    val symbol = symbol.lowercaseChar()
    return "piece_$symbol$color"
}

@SuppressLint("DiscouragedApi")
internal fun Context.getDrawable(name: String): Drawable {
    val id = resources.getIdentifier(name, "drawable", packageName)
    val drawable = AppCompatResources.getDrawable(this, id)

    return checkNotNull(drawable)
}

