package com.gaided.domain

import com.gaided.FenConverter
import com.gaided.view.chessboard.SquareNotation
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.random.Random

internal typealias Fen = String

internal class Board {
    private val _pieces = MutableSharedFlow<Map<Square, Piece>>(
        // https://github.com/Kotlin/kotlinx.coroutines/issues/2387
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        extraBufferCapacity = 1
    )
    val pieces: Flow<Map<Square, Piece>> = _pieces.asSharedFlow()

    private val _arrows = MutableSharedFlow<Set<Arrow>>(
        // https://github.com/Kotlin/kotlinx.coroutines/issues/2387
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        extraBufferCapacity = 1
    )
    val arrows: Flow<Set<Arrow>> = _arrows.asSharedFlow()

    private val fenConverter = FenConverter()

    fun generateRandomPosition() {
        val pieces = randomState()

        val whiteNotations = pieces
            .filter { it.value is Piece.WhitePawn }
            .map { it.key.notation }
            .toSet()

        _pieces.tryEmit(pieces)
        _arrows.tryEmit(randomArrows(whiteNotations))
    }

    fun setPosition(position: Fen) {
        _pieces.tryEmit(fenConverter.fromFen(position))
    }

    internal sealed class Piece {
        internal object BlackPawn : Piece()
        internal object WhitePawn : Piece()
    }

    internal data class Square(val notation: SquareNotation)

    internal data class Arrow(val start: SquareNotation, val end: SquareNotation)
}


private val rnd = Random(System.currentTimeMillis())
private fun randomState(): Map<Board.Square, Board.Piece> {
    val whiteSquares = randomSquareNotation(1, 2, 3, 4, 5)
        .map { Board.Square(it) }
    val blackSquares = randomSquareNotation(4, 5, 6, 7, 8)
        .map { Board.Square(it) }

    val whitePieces = whiteSquares.associateWith { Board.Piece.WhitePawn }
    val blackPieces = blackSquares.associateWith { Board.Piece.BlackPawn }

    return whitePieces + blackPieces
}

private fun randomArrows(from: Set<SquareNotation>): Set<Board.Arrow> {
    val numberOfArrows = 3
    return (0 until numberOfArrows)
        .map {
            val start = from.random(rnd)
            val end = randomNotation(1, 2, 3, 4, 5, 6, 7, 8)
            Board.Arrow(start, end)
        }
        .toSet()
}

private fun randomSquareNotation(vararg rows: Int): Set<SquareNotation> {
    val numberOfPieces = rnd.nextInt(8) + 4
    return (0..numberOfPieces)
        .map { randomNotation(*rows) }
        .toSet()
}

private fun randomNotation(vararg rows: Int): SquareNotation {
    val row = rows.random(rnd)
    val column = rnd.nextInt(8) + 1
    return "${"_abcdefgh"[column]}${row}"
}
