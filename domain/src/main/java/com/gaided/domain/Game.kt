@file:Suppress("RedundantVisibilityModifier")

package com.gaided.domain

public class Game(
    private val engine: Engine,
    public val board: Board = Board()
) {

    public suspend fun move(from: SquareNotation, to: SquareNotation) {
//        board.generateRandomPosition()
        val fenPosition = engine.getFenPosition()
        board.setPosition(fenPosition)
    }
}