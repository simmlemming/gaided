@file:Suppress("RedundantVisibilityModifier")

package com.gaided.domain

import kotlinx.coroutines.flow.*

public typealias FenString = String
public typealias SquareNotation = String
public typealias MoveNotation = String

public class Board {
    private val _fenPosition = MutableStateFlow(FEN_START_POSITION)
    public val fenPosition: Flow<FenString> = _fenPosition.asStateFlow()

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
