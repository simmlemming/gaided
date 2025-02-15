package com.gaided.chessgame

import com.gaided.board.stockfish.Board
import com.gaided.chessgame.ChessGame.Player.Color
import com.gaided.engine.Engine
import com.gaided.model.FenNotation
import com.gaided.model.MoveNotation
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@ExperimentalCoroutinesApi
class ChessChessGameTest {

    @Test
    fun `moves with players`() = runTest(UnconfinedTestDispatcher()) {
        // GIVEN
        val board = mockk<Board>(relaxed = true) {
            coEvery { getPosition() } returnsMany listOf(
                POSITION_AFTER_1ST_WHITE_MOVE,
                POSITION_AFTER_1ST_BLACK_MOVE,
                POSITION_AFTER_2ND_BLACK_MOVE,
                POSITION_AFTER_2ND_WHITE_MOVE,
                POSITION_AFTER_2ND_BLACK_MOVE,
            )
            coEvery { isMoveCorrect(any(), any()) } returns false
            coEvery { isMoveCorrect(any(), "a2a3") } returns true
            coEvery { isMoveCorrect(any(), "a7a6") } returns true
        }

        val game = ChessGame(board, listOf())
        val history by game.history.lastValue(backgroundScope, emptySet())
        assertTrue(history.isEmpty())

        val playerWhite = TestPlayer(Color.White)
        val playerBlack = TestPlayer(Color.Black)

        backgroundScope.launch {
            game.start(playerWhite, playerBlack)
        }

        // WHEN
        playerWhite.move("a2a3")

        // THEN
        coVerify { board.move(any(), "a2a3") }
        assertEquals(1, history.size)

        // WHEN
        playerBlack.move("a7a6")

        // THEN
        coVerify { board.move(any(), "a7a6") }
        assertEquals(2, history.size)

        // WHEN
        clearMocks(board, answers = false)
        playerWhite.move("invalid move")

        // THEN
        coVerify(exactly = 0) { board.move(any(), any()) }
        assertEquals(2, history.size)

        // WHEN
        playerWhite.move("a2a3")

        // THEN
        coVerify{ board.move(any(), "a2a3") }
        assertEquals(3, history.size)
    }

    private class TestPlayer(override val color: Color) : ChessGame.Player {
        private val _moves = Channel<MoveNotation>()

        override suspend fun getMove(): MoveNotation {
            println("Get move ${color::class.simpleName}")
            return _moves.receive()
        }

        suspend fun move(move: MoveNotation) {
            _moves.send(move)
        }
    }

    @Test
    fun move() = runTest(UnconfinedTestDispatcher()) {
        // GIVEN
        val board = mockk<Board>(relaxed = true) {
            coEvery { getPosition() } returns POSITION_AFTER_1ST_BLACK_MOVE
            coEvery { getPosition() } returns POSITION_AFTER_1ST_WHITE_MOVE
        }

        val engine = mockk<Engine>(relaxed = true)

        val game = ChessGame(board, listOf(engine))
        val history by game.history.lastValue(backgroundScope, emptySet())

        assertTrue(history.isEmpty())
        game.move("e2e4", Color.White)

        assertEquals(
            setOf(ChessGame.HalfMove(1, "e2e4", Color.White, POSITION_AFTER_1ST_WHITE_MOVE)),
            history
        )

        // WHEN
        game.move("e7e6", Color.Black)

        // THEN
        assertEquals(
            setOf(
                ChessGame.HalfMove(1, "e2e4", Color.White, POSITION_AFTER_1ST_WHITE_MOVE),
                ChessGame.HalfMove(1, "e7e6", Color.Black, FenNotation.START_POSITION)
            ),
            history
        )
    }
}

private val POSITION_AFTER_1ST_WHITE_MOVE =
    FenNotation.fromFenString("rnbqkbnr/pppppppp/8/8/8/5N2/PPPPPPPP/RNBQKB1R b KQkq - 1 1")

private val POSITION_AFTER_1ST_BLACK_MOVE =
    FenNotation.fromFenString("rnbqkbnr/pppp1ppp/4p3/8/8/5N2/PPPPPPPP/RNBQKB1R w KQkq - 0 2")

private val POSITION_AFTER_2ND_WHITE_MOVE =
    FenNotation.fromFenString("rnbqkb1r/pp2pppp/2p2n2/3p4/2PP4/4PN2/PP3PPP/RNBQKB1R b KQkq - 0 4")

private val POSITION_AFTER_2ND_BLACK_MOVE =
    FenNotation.fromFenString("rnbqkb1r/pp2pppp/2p2n2/3p4/3P4/4PN2/PPP2PPP/RNBQKB1R w KQkq - 0 4")
