package com.gaided.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import com.gaided.Game
import com.gaided.engine.Engine
import com.gaided.engine.FenNotation
import com.gaided.engine.MoveNotation
import com.gaided.engine.PieceNotation
import com.gaided.engine.SquareNotation
import com.gaided.getLastMove
import com.gaided.view.chessboard.ChessBoardView
import com.gaided.view.chessboard.ChessBoardView.State.Arrow
import com.gaided.view.chessboard.ChessBoardView.State.OverlaySquare
import com.gaided.view.player.PlayerView

internal fun toLastTopMoveArrows(player: Game.Player, topMoves: List<Engine.TopMove>): Set<Arrow> {
    val comparator = Comparator<Engine.TopMove> { o1, o2 ->
        if (player == Game.Player.White) {
            o2.centipawn - o1.centipawn
        } else {
            o1.centipawn - o2.centipawn
        }
    }

    return topMoves
        .sortedWith(comparator)
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
    topMoves: List<Engine.TopMove>
): PlayerView.State {
    val nextMovePlayer = position.toNextMovePlayer()

    return when {
        nextMovePlayer == Game.Player.None ->
            PlayerView.State.EMPTY

        nextMovePlayer == player ->
            toPlayerViewState(position, topMoves)

        nextMovePlayer == player ->
            PlayerView.State.EMPTY

        nextMovePlayer != player ->
            PlayerView.State.OPPONENT_MOVE

        nextMovePlayer != player ->
            PlayerView.State.EMPTY.copy(progressVisible = true)

        else -> PlayerView.State.EMPTY
    }
}

private fun toPlayerViewState(position: FenNotation, topMoves: List<Engine.TopMove>): PlayerView.State {
    return PlayerView.State(
        progressVisible = topMoves.isEmpty(),
        movesStats = emptyList()
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
