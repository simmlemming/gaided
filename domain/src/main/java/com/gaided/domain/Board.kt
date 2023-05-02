@file:Suppress("RedundantVisibilityModifier")

package com.gaided.domain

import kotlinx.coroutines.flow.*
import kotlin.random.Random

public typealias FenString = String
public typealias SquareNotation = String

public class Board {
    private val _fenPosition = MutableStateFlow(FEN_START_POSITION)
    public val pieces: Flow<Map<SquareNotation, Piece>> = _fenPosition.map {
        fenConverter.fromFen(it)
    }

    private val _topMovesArrows = MutableStateFlow(emptyList<Arrow>())
    public val arrows: Flow<List<Arrow>> = _topMovesArrows.asStateFlow()

    private val fenConverter = FenConverter()

    public fun setPosition(position: FenString) {
        _fenPosition.value = position
    }

    public fun setTopMoves(topMoves: List<Engine.TopMove>) {
        _topMovesArrows.value = topMoves.map {
            it.toArrow()
        }
    }

    private fun Engine.TopMove.toArrow() = Arrow(
        this.move.take(2),
        this.move.takeLast(2),
    )

    public data class Piece(public val symbol: Char) {
        public val isBlack: Boolean
            get() = symbol.isLowerCase()
    }

    public data class Arrow(val start: SquareNotation, val end: SquareNotation)
}

public const val FEN_START_POSITION: FenString =
    "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

private val rnd = Random(System.currentTimeMillis())

private fun randomState(): Map<SquareNotation, Board.Piece> {
    val whiteSquares = randomSquareNotation(1, 2, 3, 4, 5)
    val blackSquares = randomSquareNotation(4, 5, 6, 7, 8)

    val whitePieces = whiteSquares.associateWith { Board.Piece('P') }
    val blackPieces = blackSquares.associateWith { Board.Piece('p') }

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
