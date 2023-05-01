package com.gaided.domain

import com.gaided.Engine
import com.gaided.view.chessboard.SquareNotation

internal class Game(
    private val engine: Engine,
    val board: Board = Board()
) {

    fun move(from: SquareNotation, to: SquareNotation) {
        board.generateRandomPosition()
    }
}