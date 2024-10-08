package com.gaided.game

import com.gaided.engine.Engine
import com.gaided.game.Game.Player
import com.gaided.model.FenNotation
import com.gaided.board.stockfish.Board
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@ExperimentalCoroutinesApi
class GameTest {

    @Test
    fun move() = runTest(UnconfinedTestDispatcher()) {
        // GIVEN
        val board = mockk<Board>(relaxed = true) {
            coEvery { getPosition() } returns POSITION_AFTER_1ST_BLACK_MOVE
            coEvery { getPosition() } returns POSITION_AFTER_1ST_WHITE_MOVE
        }

        val engine = mockk<Engine>(relaxed = true)

        val game = Game(board, listOf(engine))
        val history by game.history.lastValue(backgroundScope, emptySet())

        assertTrue(history.isEmpty())
        game.move("e2e4", Player.White)

        assertEquals(
            setOf(Game.HalfMove(1, "e2e4", Player.White, POSITION_AFTER_1ST_WHITE_MOVE)),
            history
        )

        // WHEN
        game.move("e7e6", Player.Black)

        // THEN
        assertEquals(
            setOf(
                Game.HalfMove(1, "e2e4", Player.White, POSITION_AFTER_1ST_WHITE_MOVE),
                Game.HalfMove(1, "e7e6", Player.Black, FenNotation.START_POSITION)
            ),
            history
        )
    }
}

private val POSITION_AFTER_1ST_WHITE_MOVE =
    FenNotation.fromFenString("rnbqkbnr/pppppppp/8/8/8/5N2/PPPPPPPP/RNBQKB1R b KQkq - 1 1")

private val POSITION_AFTER_1ST_BLACK_MOVE =
    FenNotation.fromFenString("rnbqkbnr/pppp1ppp/4p3/8/8/5N2/PPPPPPPP/RNBQKB1R w KQkq - 0 2")
