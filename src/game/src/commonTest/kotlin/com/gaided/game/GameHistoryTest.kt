package com.gaided.game

import com.gaided.engine.Engine
import com.gaided.engine.FenNotation
import com.gaided.engine.Board
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
internal class GameHistoryTest {
    private lateinit var game: Game
    private lateinit var position: FenNotation

    @Before
    fun setUp() {
        val board = mockk<Board>(relaxed = true) {
            coEvery { getPosition() } answers { position }
        }

        val engine = mockk<Engine>(relaxed = true)

        game = Game(board, listOf(engine))
    }

    @Test
    fun history() = runTest(UnconfinedTestDispatcher()) {
        val history by game.history.lastValue(backgroundScope, emptySet())
        assertTrue(history.isEmpty())

        listOf(move1w, move1b, move2w, move2b).forEach {
            game.moveAndAssert(it) { history }
        }
    }

    @Test
    fun sorted() {
        assertEquals(
            listOf(move1w, move1b, move2w, move2b),
            setOf(move2w, move1w, move2b, move1b).sorted()
        )
    }

    private suspend fun Game.moveAndAssert(
        move: Game.HalfMove,
        getLastHistoryValue: () -> Set<Game.HalfMove>
    ) {
        this@GameHistoryTest.position = move.positionAfterMove
        move(move.move, move.player)
        assertEquals(move, getLastHistoryValue().getLastMove())
    }
}

private val FEN_1W = FenNotation.fromFenString("pieces1 b KQkq - 1 2")
private val FEN_1B = FenNotation.fromFenString("pieces2 w KQkq - 1 2")
private val FEN_2W = FenNotation.fromFenString("pieces3 b KQkq - 1 2")
private val FEN_2B = FenNotation.fromFenString("pieces4 w KQkq - 1 2")

private val move1w = Game.HalfMove(1, "e2e4", Game.Player.White, FEN_1W)
private val move1b = Game.HalfMove(1, "e7e6", Game.Player.Black, FEN_1B)
private val move2w = Game.HalfMove(2, "a2a3", Game.Player.White, FEN_2W)
private val move2b = Game.HalfMove(2, "h7h5", Game.Player.Black, FEN_2B)
