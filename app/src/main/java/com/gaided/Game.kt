package com.gaided

import com.gaided.domain.Board
import com.gaided.domain.Engine
import com.gaided.domain.FEN_START_POSITION
import com.gaided.domain.SquareNotation

internal class Game(
    private val engine: Engine,
    val board: Board
) {

    internal suspend fun start() {
        engine.setFenPosition(FEN_START_POSITION)
    }

    internal suspend fun move(from: SquareNotation, to: SquareNotation) {
        engine.makeMoveFromCurrentPosition(from, to)

        val fenPosition = engine.getFenPosition()
        board.setPosition(fenPosition)

        engine.getTopMoves(3)
    }
}