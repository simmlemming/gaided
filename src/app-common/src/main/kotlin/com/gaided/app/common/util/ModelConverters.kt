package com.gaided.app.common.util

import com.gaided.chessgame.ChessGame
import com.gaided.chessui.model.ChessBoardViewState.OverlaySquare
import com.gaided.model.MoveNotation
import com.gaided.model.PieceNotation
import com.gaided.model.SquareNotation

fun Map.Entry<SquareNotation, PieceNotation>.toPiece(selectedSquare: SquareNotation?, pendingMove: MoveNotation?) =
    com.gaided.chessui.model.ChessBoardViewState.Piece(
        drawableName = value.toDrawableName(),
        position = key,
        isElevated = key in setOf(selectedSquare, pendingMove?.takeLast(2))
    )

fun PieceNotation.toDrawableName(): String {
    val color = if (this.isLowerCase()) "b" else "w"
    val symbol = this.lowercaseChar()
    return "piece_$symbol$color"
}

fun MoveNotation.toLastMoveSquares() = setOf(
    OverlaySquare(this.take(2), OverlaySquare.COLOR_LAST_MOVE),
    OverlaySquare(this.takeLast(2), OverlaySquare.COLOR_LAST_MOVE)
)

fun Set<ChessGame.HalfMove>.toLastMoveSquares(): Set<OverlaySquare> {
    val lastMove = this.getLastMove() ?: return emptySet()
    return setOf(
        OverlaySquare(lastMove.move.take(2), OverlaySquare.COLOR_LAST_MOVE),
        OverlaySquare(lastMove.move.takeLast(2), OverlaySquare.COLOR_LAST_MOVE),
    )
}

private fun Set<ChessGame.HalfMove>.getLastMove(): ChessGame.HalfMove? =
    sorted().lastOrNull()

fun Set<ChessGame.HalfMove>.sorted(): List<ChessGame.HalfMove> = sortedWith { o1, o2 ->
    when {
        o1.number != o2.number -> o1.number - o2.number
        o1.player == ChessGame.Player.Color.White -> -1
        o2.player == ChessGame.Player.Color.White -> 1
        else -> 0
    }
}
