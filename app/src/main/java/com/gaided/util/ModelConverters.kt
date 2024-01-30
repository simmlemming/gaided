package com.gaided.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import com.gaided.Game
import com.gaided.domain.Engine
import com.gaided.domain.FenNotation
import com.gaided.domain.MoveNotation
import com.gaided.domain.PieceNotation
import com.gaided.domain.SquareNotation
import com.gaided.getLastMove
import com.gaided.view.chessboard.ChessBoardView
import com.gaided.view.chessboard.ChessBoardView.State.Arrow
import com.gaided.view.chessboard.ChessBoardView.State.OverlaySquare
import com.gaided.view.player.PlayerView

internal fun toLastTopMoveArrows(topMoves: List<Engine.TopMove>): Set<Arrow> {
    return topMoves
        .sortedByDescending { it.centipawn }
        .mapIndexed { index, move -> move.toArrow(Arrow.colorByTopMoveIndex(index)) }
        .toSet()
}

internal fun toTopMoveArrows(
    topMoves: List<Engine.TopMove>,
    selectedSquare: SquareNotation?,
    pendingMove: MoveNotation?
): Set<Arrow> {
    if (pendingMove != null) {
        return emptySet()
    }

    return topMoves
        .filter { selectedSquare == null || it.move.take(2) == selectedSquare }
        .map { it.toArrow(Arrow.COLOR_SUGGESTION) }
        .toSet()
}

internal fun Set<Game.HalfMove>.toLastMoveSquares(): Set<OverlaySquare> {
    val lastMove = this.getLastMove() ?: return emptySet()
    return setOf(
        OverlaySquare(lastMove.move.take(2), OverlaySquare.COLOR_LAST_MOVE),
        OverlaySquare(lastMove.move.takeLast(2), OverlaySquare.COLOR_LAST_MOVE),
    )
}

internal fun MoveNotation.toLastMoveSquares() = setOf(
    OverlaySquare(this.take(2), OverlaySquare.COLOR_LAST_MOVE),
    OverlaySquare(this.takeLast(2), OverlaySquare.COLOR_LAST_MOVE)
)

internal fun toPlayerState(
    player: Game.Player,
    position: FenNotation,
    topMoves: List<Engine.TopMove>,
    pendingMove: MoveNotation?
): PlayerView.State {
    val nextMovePlayer = position.toNextMovePlayer()

    return when {
        nextMovePlayer == Game.Player.None ->
            PlayerView.State.EMPTY

        nextMovePlayer == player && pendingMove == null ->
            toPlayerViewState(position, topMoves)

        nextMovePlayer == player && pendingMove != null ->
            PlayerView.State.EMPTY

        nextMovePlayer != player && pendingMove == null ->
            PlayerView.State.OPPONENT_MOVE

        nextMovePlayer != player && pendingMove != null ->
            PlayerView.State.EMPTY.copy(progressVisible = true)

        else -> PlayerView.State.EMPTY
    }
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
        isVisible = false,
        text = this.move,
        pieceDrawableName = position.pieceAt(this.move.take(2))?.toDrawableName()
    )
}

internal fun FenNotation.toNextMovePlayer() = when (nextMoveColor.lowercase()) {
    "w" -> Game.Player.White
    "b" -> Game.Player.Black
    else -> Game.Player.None
}

internal fun Engine.TopMove.toArrow(color: Int) = Arrow(
    start = this.move.take(2),
    end = this.move.takeLast(2),
    color = color
)

internal fun Map.Entry<SquareNotation, PieceNotation>.toPiece(selectedSquare: SquareNotation?, pendingMove: MoveNotation?) =
    ChessBoardView.State.Piece(
        drawableName = value.toDrawableName(),
        position = key,
        isElevated = key in setOf(selectedSquare, pendingMove?.takeLast(2))
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
