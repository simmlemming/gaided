package com.gaided.game

import com.gaided.engine.Engine
import com.gaided.engine.SquareNotation
import com.gaided.game.ui.model.ChessBoardViewState
import com.gaided.game.ui.model.ChessBoardViewState.Arrow
import com.gaided.game.ui.model.EvaluationViewState
import com.gaided.game.ui.model.PlayerViewState
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerifyAll
import io.mockk.confirmVerified
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

internal class GameViewModelTest : GameViewModelTestCase() {
    @Test
    fun `initial state`() = runTest {
        viewModel = createViewModelAndCollectState()

        with(viewModel.board.value) {
            assertEquals(32, pieces.size)
            assertTrue(arrows.isEmpty())
            assertTrue(overlaySquares.isEmpty())
        }

        assertEquals(PlayerViewState.EMPTY, viewModel.playerWhite.value)
        assertEquals(PlayerViewState.EMPTY, viewModel.playerBlack.value)
        assertEquals(EvaluationViewState.INITIAL, viewModel.evaluation.value)
    }

    @Test
    fun start() = runTest {
        // GIVEN
        coEvery { board.setPosition(any()) } returns Unit
        coEvery { board.getEvaluation(any()) } returns EVALUATION_50

        coEvery { engine1.getTopMoves(any(), any()) } returns TOP_MOVES_AT_START

        viewModel = createViewModelAndCollectState()

        // WHEN
        viewModel.start()

        // THEN API calls are made ...
        coVerifyAll {
            board.getEvaluation(POSITION_AT_START)
            engine1.recommendedNumberOfMoves
            engine1.getTopMoves(POSITION_AT_START, any())
        }
        confirmVerified(board, engine1)

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

        val expectedPlayerWhiteState = PlayerViewState(
            progressVisible = false,
            movesStats = emptyList()
        )

        assertEquals(expectedPlayerWhiteState, viewModel.playerWhite.value)
        assertEquals(PlayerViewState.OPPONENT_MOVE, viewModel.playerBlack.value)
        assertEquals(EvaluationViewState(50, false), viewModel.evaluation.value)
    }

    @Test
    fun onMoveClick() = runTest {
        // GIVEN
        var evaluationResponse = EVALUATION_50
        var topMovesResponse = emptyList<Engine.TopMove>()
        var positionResponse = POSITION_AT_START

        coEvery { board.setPosition(any()) } returns Unit
        coEvery { board.move(any(), any()) } returns Unit
        coEvery { board.getPosition() } answers { positionResponse }
        coEvery { board.getEvaluation(any()) } answers { evaluationResponse }

        coEvery { engine1.getTopMoves(any(), any()) } answers { topMovesResponse }

        val viewModel = createViewModelAndCollectState()
        evaluationResponse = EVALUATION_50
        topMovesResponse = TOP_MOVES_AT_START
        positionResponse = POSITION_AT_START

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
        positionResponse = POSITION_AFTER_1ST_MOVE_G1F3

        viewModel.onSquareClick("g1")

        // THEN calls are made ...
        coVerifyAll {
            // start (no need to verity here, but also no way to clear the mock)
            board.getEvaluation(POSITION_AT_START)
            engine1.recommendedNumberOfMoves
            engine1.getTopMoves(POSITION_AT_START, any())

            // move
            board.move(POSITION_AT_START, "g1f3")
            board.getPosition()
            board.getEvaluation(POSITION_AFTER_1ST_MOVE_G1F3)
            engine1.recommendedNumberOfMoves
            engine1.getTopMoves(POSITION_AFTER_1ST_MOVE_G1F3, any())
        }
        confirmVerified(board, engine1)

        // ... and piece is moved
        assertNull(viewModel.board.pieceAt("g1"))
        assertEquals(
            PIECE_WHITE_KNIGHT_AT_F3,
            viewModel.board.pieceAt("f3")
        )

        // ... and state is updated
        assertEquals(
            EvaluationViewState(150, false),
            viewModel.evaluation.value
        )
        assertEquals(
            PlayerViewState.OPPONENT_MOVE,
            viewModel.playerWhite.value
        )
        assertEquals(
            PlayerViewState(
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
            ChessBoardViewState.OverlaySquare("g1", ChessBoardViewState.OverlaySquare.COLOR_LAST_MOVE),
            ChessBoardViewState.OverlaySquare("f3", ChessBoardViewState.OverlaySquare.COLOR_LAST_MOVE)
        )

        assertEquals(expectedOverlaySquares, viewModel.board.value.overlaySquares)
    }

    private fun StateFlow<ChessBoardViewState>.pieceAt(square: SquareNotation) =
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
