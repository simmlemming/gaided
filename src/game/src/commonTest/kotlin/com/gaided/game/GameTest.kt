package com.gaided.game

import com.gaided.engine.Engine
import com.gaided.engine.FenNotation
import com.gaided.engine.RemoteBoard
import com.gaided.game.Game.Player
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
        val remoteBoard = mockk<RemoteBoard>(relaxed = true) {
            coEvery { getFenPosition() } returns FEN_NOTATION_AFTER_1ST_BLACK_MOVE.fenString
            coEvery { getFenPosition() } returns FEN_NOTATION_AFTER_1ST_WHITE_MOVE.fenString
        }

        val engine = mockk<Engine>(relaxed = true)

        val game = Game(remoteBoard, listOf(engine))
        val history by game.history.lastValue(backgroundScope, emptySet())

        assertTrue(history.isEmpty())
        game.move("e2e4", Player.White)

        assertEquals(
            setOf(Game.HalfMove(1, "e2e4", Player.White, FEN_NOTATION_AFTER_1ST_WHITE_MOVE)),
            history
        )

        // WHEN
        game.move("e7e6", Player.Black)

        // THEN
        assertEquals(
            setOf(
                Game.HalfMove(1, "e2e4", Player.White, FEN_NOTATION_AFTER_1ST_WHITE_MOVE),
                Game.HalfMove(1, "e7e6", Player.Black, FenNotation.START_POSITION)
            ),
            history
        )
    }
}

private val FEN_NOTATION_AFTER_1ST_WHITE_MOVE =
    FenNotation.fromFenString("rnbqkbnr/pppppppp/8/8/8/5N2/PPPPPPPP/RNBQKB1R b KQkq - 1 1")

private val FEN_NOTATION_AFTER_1ST_BLACK_MOVE =
    FenNotation.fromFenString("rnbqkbnr/pppp1ppp/4p3/8/8/5N2/PPPPPPPP/RNBQKB1R w KQkq - 0 2")
