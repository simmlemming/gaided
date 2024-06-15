package com.gaided.game.util

import com.gaided.engine.FenNotation
import com.gaided.engine.MoveNotation
import com.gaided.engine.PieceNotation
import com.gaided.engine.RemoteBoard
import com.gaided.engine.SquareNotation
import com.gaided.game.Game
import com.gaided.game.getLastMove
import com.gaided.game.ui.model.ChessBoardViewState
import com.gaided.game.ui.model.ChessBoardViewState.Arrow
import com.gaided.game.ui.model.ChessBoardViewState.OverlaySquare
import com.gaided.game.ui.model.PlayerViewState

@Suppress("kotlin:S1135")
internal fun toLastTopMoveArrows(player: Game.Player, topMoves: List<RemoteBoard.TopMove>): Set<Arrow> {
    // TODO: Need to sort this list, or it is always sorted?
    //  Top moves from AI engines do not have centipawn evaluations.
    val comparator = Comparator<RemoteBoard.TopMove> { o1, o2 ->
        if (player == Game.Player.White) {
            (o2.centipawn ?: 0) - (o1.centipawn ?: 0)
        } else {
            (o1.centipawn ?: 0) - (o2.centipawn ?: 0)
        }
    }

    return topMoves
        .sortedWith(comparator)
        .mapIndexed { index, move -> move.toArrow(Arrow.colorByTopMoveIndex(index)) }
        .toSet()
}

internal fun toTopMoveArrows(
    topMoves: List<RemoteBoard.TopMove>,
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
    topMoves: List<RemoteBoard.TopMove>
): PlayerViewState {
    val nextMovePlayer = position.toNextMovePlayer()

    return when {
        nextMovePlayer == Game.Player.None ->
            PlayerViewState.EMPTY

        nextMovePlayer == player ->
            toPlayerViewState(position, topMoves)

        nextMovePlayer == player ->
            PlayerViewState.EMPTY

        nextMovePlayer != player ->
            PlayerViewState.OPPONENT_MOVE

        nextMovePlayer != player ->
            PlayerViewState.EMPTY.copy(progressVisible = true)

        else -> PlayerViewState.EMPTY
    }
}

private fun toPlayerViewState(position: FenNotation, topMoves: List<RemoteBoard.TopMove>): PlayerViewState {
    return PlayerViewState(
        progressVisible = topMoves.isEmpty(),
        movesStats = emptyList()
    )
}

internal fun FenNotation.toNextMovePlayer() = when (nextMoveColor.lowercase()) {
    "w" -> Game.Player.White
    "b" -> Game.Player.Black
    else -> Game.Player.None
}

internal fun RemoteBoard.TopMove.toArrow(color: Int) = Arrow(
    start = this.move.take(2),
    end = this.move.takeLast(2),
    color = color
)

internal fun Map.Entry<SquareNotation, PieceNotation>.toPiece(selectedSquare: SquareNotation?, pendingMove: MoveNotation?) =
    ChessBoardViewState.Piece(
        drawableName = value.toDrawableName(),
        position = key,
        isElevated = key in setOf(selectedSquare, pendingMove?.takeLast(2))
    )

private fun PieceNotation.toDrawableName(): String {
    val color = if (this.isLowerCase()) "b" else "w"
    val symbol = this.lowercaseChar()
    return "piece_$symbol$color"
}