package com.gaided.game.util

import com.gaided.engine.Engine
import com.gaided.game.Game
import com.gaided.game.ui.model.ChessBoardViewState.Arrow
import org.junit.Test
import kotlin.test.assertEquals

class ToLastTopMovesTest {
    @Test
    fun `test arrows for black without evaluation`() {
        val arrows = toLastTopMoveArrows(
            Game.Player.Black,
            listOf(
                Engine.TopMove("e1", "a2a4", 100),
                Engine.TopMove("e1", "a2a3", 50),
                Engine.TopMove("e2", "b2b4"),
                Engine.TopMove("e2", "b2b3"),
            )
        )

        assertEquals(
            setOf(
                Arrow("a2", "a3", Arrow.colorByTopMoveIndex(0)),
                Arrow("a2", "a4", Arrow.colorByTopMoveIndex(1)),
                Arrow("b2", "b4", Arrow.COLOR_SUGGESTION),
                Arrow("b2", "b3", Arrow.COLOR_SUGGESTION),
            ),
            arrows
        )
    }

    @Test
    fun `test arrows for white without evaluation`() {
        val arrows = toLastTopMoveArrows(
            Game.Player.White,
            listOf(
                Engine.TopMove("e1", "a2a4", 100),
                Engine.TopMove("e1", "a2a3", 50),
                Engine.TopMove("e2", "b2b4"),
                Engine.TopMove("e2", "b2b3"),
            )
        )

        assertEquals(
            setOf(
                Arrow("a2", "a4", Arrow.colorByTopMoveIndex(0)),
                Arrow("a2", "a3", Arrow.colorByTopMoveIndex(1)),
                Arrow("b2", "b4", Arrow.COLOR_SUGGESTION),
                Arrow("b2", "b3", Arrow.COLOR_SUGGESTION),
            ),
            arrows
        )
    }

    @Test
    fun `test arrows for white`() {
        val arrows = toLastTopMoveArrows(
            Game.Player.White,
            listOf(
                Engine.TopMove("e1", "a2a4", 100),
                Engine.TopMove("e1", "a2a3", 50),
            )
        )

        assertEquals(
            setOf(
                Arrow("a2", "a4", Arrow.colorByTopMoveIndex(0)),
                Arrow("a2", "a3", Arrow.colorByTopMoveIndex(1)),
            ),
            arrows
        )
    }

    @Test
    fun `test arrows for black`() {
        val arrows = toLastTopMoveArrows(
            Game.Player.Black,
            listOf(
                Engine.TopMove("e1", "a2a4", 100),
                Engine.TopMove("e1", "a2a3", 50),
            )
        )

        assertEquals(
            setOf(
                Arrow("a2", "a3", Arrow.colorByTopMoveIndex(0)),
                Arrow("a2", "a4", Arrow.colorByTopMoveIndex(1)),
            ),
            arrows
        )
    }
}