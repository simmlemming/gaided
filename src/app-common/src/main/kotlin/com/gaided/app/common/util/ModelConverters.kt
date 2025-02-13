package com.gaided.app.common.util

import com.gaided.model.MoveNotation
import com.gaided.model.PieceNotation
import com.gaided.model.SquareNotation

fun Map.Entry<SquareNotation, PieceNotation>.toPiece(selectedSquare: SquareNotation?, pendingMove: MoveNotation?) =
    com.gaided.chessui. model.ChessBoardViewState.Piece(
        drawableName = value.toDrawableName(),
        position = key,
        isElevated = key in setOf(selectedSquare, pendingMove?.takeLast(2))
    )

fun PieceNotation.toDrawableName(): String {
    val color = if (this.isLowerCase()) "b" else "w"
    val symbol = this.lowercaseChar()
    return "piece_$symbol$color"
}