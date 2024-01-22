package com.gaided

import app.cash.turbine.test
import com.gaided.Game.Player
import com.gaided.domain.Engine
import com.gaided.domain.FenNotation
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@ExperimentalCoroutinesApi
class GameTest {

    @Test
    fun move() = runTest {
        val engine = mockk<Engine>(relaxed = true) {
            coEvery { getFenPosition() } returns POSITION_AFTER_1ST_BLACK_MOVE
            coEvery { getFenPosition() } returns POSITION_AFTER_1ST_WHITE_MOVE
        }

        val game = Game(engine)
        game.start()
        advanceUntilIdle()

        game.history.test {
            assertTrue(awaitItem().isEmpty())
            game.move(Player.White, "e2e4")
            advanceUntilIdle()

            assertEquals(
                setOf(Game.HalfMove(1, "e2e4", Player.White, FenNotation.fromFenString(POSITION_AFTER_1ST_WHITE_MOVE))),
                awaitItem()
            )

            game.move(Player.Black, "e7e6")
            advanceUntilIdle()

            assertEquals(
                setOf(
                    Game.HalfMove(1, "e2e4", Player.White, FenNotation.fromFenString(POSITION_AFTER_1ST_WHITE_MOVE)),
                    Game.HalfMove(1, "e7e6", Player.Black, FenNotation.START_POSITION)
                ),
                awaitItem()
            )
        }
    }
}

@Suppress("PrivatePropertyName")
private val POSITION_AFTER_1ST_WHITE_MOVE =
    "rnbqkbnr/pppppppp/8/8/8/5N2/PPPPPPPP/RNBQKB1R b KQkq - 1 1"

@Suppress("PrivatePropertyName")
private val POSITION_AFTER_1ST_BLACK_MOVE =
    "rnbqkbnr/pppp1ppp/4p3/8/8/5N2/PPPPPPPP/RNBQKB1R w KQkq - 0 2"
