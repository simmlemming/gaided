package com.gaided.game.ui.model

import androidx.compose.runtime.Immutable
import com.gaided.engine.SquareNotation
import com.gaided.game.util.Color

@Immutable
data class ChessBoardViewState(
    val pieces: Set<Piece>,
    val arrows: Set<Arrow>,
    val overlaySquares: Set<OverlaySquare>
) {
    companion object {
        val EMPTY = ChessBoardViewState(setOf(), setOf(), setOf())
    }

    @Immutable
    data class Piece(
        val drawableName: String,
        val position: SquareNotation,
        val isElevated: Boolean = false
    )

    @Immutable
    data class Arrow(
        val start: SquareNotation,
        val end: SquareNotation,
        val color: Int,
        val weight: Float = 1f,
        val strong: Boolean = true
    ) {

        companion object {
            internal const val COLOR_SUGGESTION: Int = Color.GRAY

            fun colorByTopMoveIndex(index: Int) = when (index) {
                0 -> Color.GREEN
                1 -> Color.YELLOW
                else -> Color.RED
            }
        }
    }

    @Immutable
    data class OverlaySquare(
        val square: SquareNotation,
        val color: Int
    ) {
        companion object {
            const val COLOR_LAST_MOVE = Color.YELLOW
            const val COLOR_HIGHLIGHT = Color.RED
        }
    }
}
