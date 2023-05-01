@file:Suppress("RedundantVisibilityModifier")

package com.gaided.domain

public class Game(
    private val engine: Engine,
    public val board: Board = Board()
) {

    public fun move(from: SquareNotation, to: SquareNotation) {
        board.generateRandomPosition()
    }
}