@file:Suppress("RedundantVisibilityModifier")

package com.gaided.domain

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.random.Random

internal typealias FenString = String
public typealias SquareNotation = String

public class Board {
    private val _pieces = MutableSharedFlow<Map<SquareNotation, Piece>>(
        // https://github.com/Kotlin/kotlinx.coroutines/issues/2387
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        extraBufferCapacity = 1
    )
    public val pieces: Flow<Map<SquareNotation, Piece>> = _pieces.asSharedFlow()

    private val _arrows = MutableSharedFlow<Set<Arrow>>(
        // https://github.com/Kotlin/kotlinx.coroutines/issues/2387
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        extraBufferCapacity = 1
    )
    public val arrows: Flow<Set<Arrow>> = _arrows.asSharedFlow()

    private val fenConverter = FenConverter()

    internal fun generateRandomPosition() {
        val pieces = randomState()

        val whiteNotations = pieces
            .filter { !it.value.isBlack }
            .map { it.key }
            .toSet()

        _pieces.tryEmit(pieces)
        _arrows.tryEmit(randomArrows(whiteNotations))
    }

    internal fun setPosition(position: FenString) {
        _pieces.tryEmit(fenConverter.fromFen(position))
        _arrows.tryEmit(emptySet())
    }

    public data class Piece(public val symbol: Char) {
        public val isBlack: Boolean
            get() = symbol.isLowerCase()
    }

    public data class Arrow(val start: SquareNotation, val end: SquareNotation)
}


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
