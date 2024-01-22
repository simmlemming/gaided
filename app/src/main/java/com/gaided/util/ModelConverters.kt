package com.gaided.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import com.gaided.Game
import com.gaided.domain.Engine
import com.gaided.domain.FenNotation
import com.gaided.domain.PieceNotation
import com.gaided.domain.SquareNotation
import com.gaided.view.chessboard.ChessBoardView
import com.gaided.view.player.PlayerView


internal fun toPlayerState(
    player: Game.Player,
    position: FenNotation,
    topMoves: Map<FenNotation, List<Engine.TopMove>>
) = when (position.toNextMovePlayer()) {
    Game.Player.None -> PlayerView.State.EMPTY
    player -> toPlayerViewState(position, topMoves.getOrDefault(position, emptyList()))
    else -> PlayerView.State.OPPONENT_MOVE
}

private fun toPlayerViewState(position: FenNotation, topMoves: List<Engine.TopMove>): PlayerView.State {
    return PlayerView.State(
        progressVisible = topMoves.isEmpty(),
        move1 = topMoves.getOrNull(0).toPlayerViewMoveState(position),
        move2 = topMoves.getOrNull(1).toPlayerViewMoveState(position),
        move3 = topMoves.getOrNull(2).toPlayerViewMoveState(position)
    )
}

private fun Engine.TopMove?.toPlayerViewMoveState(position: FenNotation): PlayerView.State.Move {
    this?.move ?: return PlayerView.State.Move("", false, "", null)

    return PlayerView.State.Move(
        move = this.move,
        isVisible = true,
        text = this.move,
        pieceDrawableName = position.pieceAt(this.move.take(2))?.toDrawableName()
    )
}

internal fun FenNotation.toNextMovePlayer() = when (nextMoveColor.lowercase()) {
    "w" -> Game.Player.White
    "b" -> Game.Player.Black
    else -> Game.Player.None
}

internal fun Engine.TopMove.toArrow() = ChessBoardView.State.Arrow(
    start = this.move.take(2),
    end = this.move.takeLast(2),
    color = ChessBoardView.State.Arrow.COLOR_SUGGESTION
)

internal fun Map.Entry<SquareNotation, PieceNotation>.toPiece() = ChessBoardView.State.Piece(
    drawableName = value.toDrawableName(),
    position = key,
)

private fun PieceNotation.toDrawableName(): String {
    val color = if (this.isLowerCase()) "b" else "w"
    val symbol = this.lowercaseChar()
    return "piece_$symbol$color"
}

@SuppressLint("DiscouragedApi")
internal fun Context.getDrawable(name: String): Drawable {
    val id = resources.getIdentifier(name, "drawable", packageName)
    val drawable = AppCompatResources.getDrawable(this, id)

    return checkNotNull(drawable)
}
