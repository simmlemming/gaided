package com.gaided

import com.gaided.domain.Engine
import com.gaided.domain.FenNotation
import com.gaided.domain.SquareNotation
import com.gaided.domain.api.StockfishApi
import com.gaided.view.chessboard.ChessBoardView
import com.gaided.view.chessboard.ChessBoardView.State.Arrow
import com.gaided.view.evaluation.EvaluationView
import com.gaided.view.player.PlayerView
import com.gaided.view.player.PlayerView.State.Move
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifySequence
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class GameViewModelTest {
    private lateinit var api: StockfishApi
    private lateinit var viewModel: GameViewModel
    private lateinit var testDispatcher: TestDispatcher

    @Before
    fun setUp() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
    }

    @Test
    fun `initial state`() = runTest {
        api = mockk()
        viewModel = createViewModel(backgroundScope)

        advanceUntilIdle()
        with(viewModel.board.value) {
            assertEquals(32, pieces.size)
            assertTrue(arrows.isEmpty())
            assertTrue(overlaySquares.isEmpty())
        }

        assertEquals(PlayerView.State.EMPTY, viewModel.playerWhite.value)
        assertEquals(PlayerView.State.EMPTY, viewModel.playerBlack.value)
        assertEquals(EvaluationView.State.INITIAL, viewModel.evaluation.value)
    }

    @Test
    fun start() = runTest {
        api = mockk {
            every { setFenPosition(any()) } returns Unit
            every { getEvaluation() } returns EVALUATION_50
            every { getTopMoves(any()) } returns TOP_MOVES_AT_START
        }
        viewModel = createViewModel(backgroundScope)

        viewModel.start()
        advanceUntilIdle()

        verifySequence {
            api.setFenPosition(FEN_POSITION_AT_START)
            api.getEvaluation()
            api.setFenPosition(FEN_POSITION_AT_START)
            api.getTopMoves(any())
        }
        confirmVerified(api)

        val expectedArrows = setOf(
            Arrow("d2", "d4", Arrow.COLOR_SUGGESTION),
            Arrow("g1", "f3", Arrow.COLOR_SUGGESTION),
            Arrow("e2", "e4", Arrow.COLOR_SUGGESTION),
        )

        with(viewModel.board.value) {
            assertEquals(32, pieces.size)
            assertEquals(expectedArrows, arrows)
            assertTrue(overlaySquares.isEmpty())
        }

        val expectedPlayerWhiteState = PlayerView.State(
            progressVisible = false,
            move1 = Move("d2d4", true, "d2d4", "piece_pw"),
            move2 = Move("g1f3", true, "g1f3", "piece_nw"),
            move3 = Move("e2e4", true, "e2e4", "piece_pw")
        )

        assertEquals(expectedPlayerWhiteState, viewModel.playerWhite.value)
        assertEquals(PlayerView.State.OPPONENT_MOVE, viewModel.playerBlack.value)
        assertEquals(EvaluationView.State(50, false), viewModel.evaluation.value)
    }

    @Test
    fun onMoveClick() = runTest {
        var evaluationResponse = ""
        var topMovesResponse = "[]"
        var fenPositionResponse = "[]"

        api = mockk {
            every { setFenPosition(any()) } returns Unit
            every { makeMovesFromCurrentPosition(any()) } returns Unit
            every { getFenPosition() } answers {
                fenPositionResponse
            }
            every { getEvaluation() } answers {
                evaluationResponse
            }
            every { getTopMoves(any()) } answers {
                topMovesResponse
            }
        }

        val viewModel = createViewModel(backgroundScope)
        evaluationResponse = EVALUATION_50
        topMovesResponse = TOP_MOVES_AT_START
        fenPositionResponse = FEN_POSITION_AT_START

        viewModel.start()
        advanceUntilIdle()
        clearRecordedCalls()

        assertEquals(
            WHITE_KNIGHT_AT_G1,
            viewModel.board.pieceAt("g1")
        )
        assertNull(viewModel.board.pieceAt("f3"))

        // WHEN
        evaluationResponse = EVALUATION_150
        topMovesResponse = TOP_MOVES_AFTER_1ST_MOVE
        fenPositionResponse = FEN_POSITION_AFTER_1ST_MOVE

        viewModel.onMoveClick(Game.Player.White, "g1f3")
        advanceUntilIdle()

        // THEN calls are made ...
        verifySequence {
            // start (no need to verity here, but also no way to clear the mock)
            api.setFenPosition(FEN_POSITION_AT_START)
            api.getEvaluation()
            api.setFenPosition(FEN_POSITION_AT_START)
            api.getTopMoves(any())

            // move
            api.setFenPosition(FEN_POSITION_AT_START)
            api.makeMovesFromCurrentPosition(listOf("g1f3"))
            api.getFenPosition()
            api.setFenPosition(FEN_POSITION_AFTER_1ST_MOVE)
            api.getEvaluation()
            api.setFenPosition(FEN_POSITION_AFTER_1ST_MOVE)
            api.getTopMoves(any())
        }
        confirmVerified(api)

        // ... and piece is moved
        assertNull(viewModel.board.pieceAt("g1"))
        assertEquals(
            WHITE_KNIGHT_AT_F3,
            viewModel.board.pieceAt("f3")
        )

        // ... and state is updated
        assertEquals(
            EvaluationView.State(150, false),
            viewModel.evaluation.value
        )
        assertEquals(
            PlayerView.State.OPPONENT_MOVE,
            viewModel.playerWhite.value
        )
        assertEquals(
            PlayerView.State(
                progressVisible = false,
                move1 = Move("e7e6", true, "e7e6", "piece_pb"),
                move2 = Move("", false, "", null),
                move3 = Move("", false, "", null)
            ),
            viewModel.playerBlack.value
        )
        assertEquals(
            setOf(Arrow("e7", "e6", Arrow.COLOR_SUGGESTION)),
            viewModel.board.value.arrows
        )

        // ... and last move is highlighted
        val expectedOverlaySquares = setOf(
            ChessBoardView.State.OverlaySquare("g1", ChessBoardView.State.OverlaySquare.COLOR_LAST_MOVE),
            ChessBoardView.State.OverlaySquare("f3", ChessBoardView.State.OverlaySquare.COLOR_LAST_MOVE)
        )

        assertEquals(expectedOverlaySquares, viewModel.board.value.overlaySquares)
    }

    private fun StateFlow<ChessBoardView.State>.pieceAt(square: SquareNotation) =
        value.pieces.firstOrNull { it.position == square }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(scope: CoroutineScope) = GameViewModel(Game(Engine(api, testDispatcher)))
        .also {
            scope.launch { it.board.collect() }
            scope.launch { it.evaluation.collect() }
            scope.launch { it.playerWhite.collect() }
            scope.launch { it.playerBlack.collect() }
        }

    private fun clearRecordedCalls() {
        clearAllMocks(
            answers = false,
            recordedCalls = true,
            childMocks = false,
            regularMocks = false,
            objectMocks = false,
            staticMocks = false,
            constructorMocks = false,
        )
    }
}

private const val EVALUATION_50 = "{\"type\": \"cp\", \"value\": 50}"
private const val EVALUATION_150 = "{\"type\": \"cp\", \"value\": 150}"

@Suppress("PrivatePropertyName")
private val TOP_MOVES_AT_START = """
                [
                    {'Move': 'd2d4', 'Centipawn': 29, 'Mate': None},
                    {'Move': 'g1f3', 'Centipawn': 25, 'Mate': None},
                    {'Move': 'e2e4', 'Centipawn': 23, 'Mate': None}
                ]
            """.trimIndent()

@Suppress("PrivatePropertyName")
private val TOP_MOVES_AFTER_1ST_MOVE = """
                [
                    {'Move': 'e7e6', 'Centipawn': 23, 'Mate': None}
                ]
            """.trimIndent()

@Suppress("PrivatePropertyName")
private val FEN_POSITION_AT_START =
    FenNotation.START_POSITION.fenString

private const val FEN_POSITION_AFTER_1ST_MOVE =
    "rnbqkbnr/pppppppp/8/8/8/5N2/PPPPPPPP/RNBQKB1R b KQkq - 1 1"

@Suppress("PrivatePropertyName")
private val WHITE_KNIGHT_AT_G1 = ChessBoardView.State.Piece("piece_nw", "g1")

@Suppress("PrivatePropertyName")
private val WHITE_KNIGHT_AT_F3 = ChessBoardView.State.Piece("piece_nw", "f3")