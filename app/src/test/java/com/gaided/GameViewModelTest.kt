package com.gaided

import com.gaided.domain.SquareNotation
import com.gaided.view.chessboard.ChessBoardView
import com.gaided.view.chessboard.ChessBoardView.State.Arrow
import com.gaided.view.evaluation.EvaluationView
import com.gaided.view.player.PlayerView
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerifyAll
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

internal class GameViewModelTest : GameViewModelTestCase() {
    @Test
    fun `initial state`() = runTest {
        api = mockk()
        viewModel = createViewModelAndCollectState()

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
        // GIVEN
        api = mockk {
            coEvery { setFenPosition(any()) } returns Unit
            coEvery { getEvaluation(any()) } returns EVALUATION_50
            coEvery { getTopMoves(any(), any()) } returns TOP_MOVES_AT_START
        }
        viewModel = createViewModelAndCollectState()

        // WHEN
        viewModel.start()

        // THEN API calls are made ...
        coVerifyAll {
            api.getEvaluation(FEN_POSITION_AT_START)
            api.getTopMoves(FEN_POSITION_AT_START, any())
        }
        confirmVerified(api)

        // ... and state is correct
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
            movesStats = emptyList()
        )

        assertEquals(expectedPlayerWhiteState, viewModel.playerWhite.value)
        assertEquals(PlayerView.State.OPPONENT_MOVE, viewModel.playerBlack.value)
        assertEquals(EvaluationView.State(50, false), viewModel.evaluation.value)
    }

    @Test
    fun onMoveClick() = runTest {
        // GIVEN
        var evaluationResponse = ""
        var topMovesResponse = "[]"
        var fenPositionResponse = "[]"

        api = mockk {
            coEvery { setFenPosition(any()) } returns Unit
            coEvery { makeMoves(any(), any()) } returns Unit
            coEvery { getFenPosition() } answers {
                fenPositionResponse
            }
            coEvery { getEvaluation(any()) } answers {
                evaluationResponse
            }
            coEvery { getTopMoves(any(), any()) } answers {
                topMovesResponse
            }
        }

        val viewModel = createViewModelAndCollectState()
        evaluationResponse = EVALUATION_50
        topMovesResponse = TOP_MOVES_AT_START
        fenPositionResponse = FEN_POSITION_AT_START

        viewModel.start()
        clearRecordedCalls()

        assertEquals(
            PIECE_WHITE_KNIGHT_AT_G1,
            viewModel.board.pieceAt("g1")
        )
        assertNull(viewModel.board.pieceAt("f3"))

        // WHEN a square with one top move is clicked
        evaluationResponse = EVALUATION_150
        topMovesResponse = TOP_MOVES_AFTER_1ST_MOVE
        fenPositionResponse = FEN_POSITION_AFTER_1ST_MOVE_G1F3

        viewModel.onSquareClick("g1")

        // THEN calls are made ...
        coVerifyAll {
            // start (no need to verity here, but also no way to clear the mock)
            api.getEvaluation(FEN_POSITION_AT_START)
            api.getTopMoves(FEN_POSITION_AT_START, any())

            // move
            api.makeMoves(FEN_POSITION_AT_START, listOf("g1f3"))
            api.getFenPosition()
            api.getEvaluation(FEN_POSITION_AFTER_1ST_MOVE_G1F3)
            api.getTopMoves(FEN_POSITION_AFTER_1ST_MOVE_G1F3, any())
        }
        confirmVerified(api)

        // ... and piece is moved
        assertNull(viewModel.board.pieceAt("g1"))
        assertEquals(
            PIECE_WHITE_KNIGHT_AT_F3,
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
                movesStats = emptyList()
            ),
            viewModel.playerBlack.value
        )
        assertEquals(
            setOf(
                Arrow("e7", "e6", Arrow.COLOR_SUGGESTION),
                Arrow("d2", "d4", Arrow.colorByTopMoveIndex(0)),
                Arrow("g1", "f3", Arrow.colorByTopMoveIndex(1)),
                Arrow("e2", "e4", Arrow.colorByTopMoveIndex(2))
            ),
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
