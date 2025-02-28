package com.gaided.chessgame

import com.gaided.board.stockfish.Board
import com.gaided.chessgame.ChessGame.Player.Color
import com.gaided.chessgame.ChessGame.State
import com.gaided.engine.Engine
import com.gaided.model.FenNotation
import com.gaided.model.MoveNotation
import com.gaided.model.toMove
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@ExperimentalCoroutinesApi
class ChessGameTest {

    @Test
    fun state() = runTest(UnconfinedTestDispatcher(), timeout = 5.seconds) {
        // GIVEN
        val board = mockBoard()

        val playerWhite = TestPlayer(Color.White)
        val playerBlack = TestPlayer(Color.Black)

        val game = ChessGame(board, listOf())
        val state by game.state.lastValue(backgroundScope, null)
        val history by game.history.lastValue(backgroundScope, emptySet())

        // THEN
        assertIs<State.Created>(state)

        // WHEN game is started
        backgroundScope.launch {
            game.play(playerWhite, playerBlack)
        }

        // THEN
        assertEquals(0, history.size)
        state.assertWaitingForMove(
            Color.White, FenNotation.START_POSITION, EVALUATION_START_POSITION
        )

        // 1st white move
        playerWhite.move("a2a3".toMove())
        assertEquals(1, history.size)
        state.assertWaitingForMove(
            Color.Black, POSITION_AFTER_1ST_WHITE_MOVE, EVALUATION_AFTER_1ST_WHITE_MOVE
        )

        // 1st black move
        playerBlack.move("a7a6".toMove())
        assertEquals(2, history.size)
        state.assertWaitingForMove(
            Color.White, POSITION_AFTER_1ST_BLACK_MOVE, EVALUATION_AFTER_1ST_BLACK_MOVE
        )

        // 2nd white move -> state: mate in 1
        playerWhite.move("a2a3".toMove())
        assertEquals(3, history.size)
        state.assertWaitingForMove(
            Color.Black, POSITION_AFTER_2ND_WHITE_MOVE, EVALUATION_AFTER_2ND_WHITE_MOVE
        )

        // 2nd black move -> state: mate
        playerBlack.move("a7a6".toMove())
        assertEquals(4, history.size)
        state.assertFinished(
            Color.Black, POSITION_AFTER_2ND_BLACK_MOVE
        )
    }

    private fun State?.assertFinished(winner: Color, position: FenNotation) =
        assertEquals(State.Finished(winner, position), this)

    private fun State?.assertWaitingForMove(color: Color, position: FenNotation, evaluation: Board.Evaluation) =
        assertEquals(State.WaitingForMove(color, position, evaluation), this)

    private fun mockBoard() = mockk<Board> {
        coEvery { move(any(), any()) } just Runs

        coEvery { getPosition() } returnsMany listOf(
            POSITION_AFTER_1ST_WHITE_MOVE,
            POSITION_AFTER_1ST_BLACK_MOVE,
            POSITION_AFTER_2ND_WHITE_MOVE,
            POSITION_AFTER_2ND_BLACK_MOVE,
        )

        coEvery { isMoveCorrect(any(), any()) } returns false
        coEvery { isMoveCorrect(any(), "a2a3".toMove()) } returns true
        coEvery { isMoveCorrect(any(), "a7a6".toMove()) } returns true

        mapOf(
            FenNotation.START_POSITION to EVALUATION_START_POSITION,
            POSITION_AFTER_1ST_WHITE_MOVE to EVALUATION_AFTER_1ST_WHITE_MOVE,
            POSITION_AFTER_1ST_BLACK_MOVE to EVALUATION_AFTER_1ST_BLACK_MOVE,
            POSITION_AFTER_2ND_WHITE_MOVE to EVALUATION_AFTER_2ND_WHITE_MOVE,
            POSITION_AFTER_2ND_BLACK_MOVE to EVALUATION_AFTER_2ND_BLACK_MOVE,
        ).forEach {
            coEvery { getEvaluation(it.key) } returns it.value
        }
    }

    private class TestPlayer(override val color: Color) : ChessGame.Player {
        override val name = "Test player"

        private val _moves = Channel<MoveNotation>()

        override suspend fun getMove(position: FenNotation): MoveNotation? {
            println("Get move ${color::class.simpleName}")
            val received = _moves.receive()
            println("... move = $received")
            return received
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
        game.move("e2e4".toMove(), Color.White)

        assertEquals(
            setOf(ChessGame.HalfMove(1, "e2e4".toMove(), Color.White, POSITION_AFTER_1ST_WHITE_MOVE)),
            history
        )

        // WHEN
        game.move("e7e6".toMove(), Color.Black)

        // THEN
        assertEquals(
            setOf(
                ChessGame.HalfMove(1, "e2e4".toMove(), Color.White, POSITION_AFTER_1ST_WHITE_MOVE),
                ChessGame.HalfMove(1, "e7e6".toMove(), Color.Black, FenNotation.START_POSITION)
            ),
            history
        )
    }
}

private val POSITION_AFTER_1ST_WHITE_MOVE =
    FenNotation.fromFenString("1nbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b KQkq - 1 1")

private val POSITION_AFTER_1ST_BLACK_MOVE =
    FenNotation.fromFenString("2bqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 1 1")

private val POSITION_AFTER_2ND_WHITE_MOVE =
    FenNotation.fromFenString("3qkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b KQkq - 1 1")

private val POSITION_AFTER_2ND_BLACK_MOVE =
    FenNotation.fromFenString("4kbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 1 1")

private val EVALUATION_START_POSITION = Board.Evaluation("cp", 0)
private val EVALUATION_AFTER_1ST_WHITE_MOVE = Board.Evaluation("cp", 100)
private val EVALUATION_AFTER_1ST_BLACK_MOVE = Board.Evaluation("cp", -100)
private val EVALUATION_AFTER_2ND_WHITE_MOVE = Board.Evaluation("mate", 1)
private val EVALUATION_AFTER_2ND_BLACK_MOVE = Board.Evaluation("mate", 0)