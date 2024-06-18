package com.gaided.game

import com.gaided.engine.Engine
import com.gaided.engine.SquareNotation
import com.gaided.game.ui.model.ChessBoardViewState
import com.gaided.game.ui.model.ChessBoardViewState.Arrow
import io.mockk.Runs
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.just
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@ExperimentalCoroutinesApi
internal class GameViewModelOnSquareClickTest : GameViewModelTestCase() {

    @Test
    fun `empty square`() = runTest {
        // GIVEN
        val viewModel = createViewModelAndCollectState()
        assertNull(
            viewModel.board["a4"]
        )
        assertNull(
            viewModel.board["a3"]
        )

        // WHEN empty squares are clicked
        viewModel.onSquareClick("a4")
        viewModel.onSquareClick("a3")
        advanceUntilIdle()

        // THEN nothing happens
        confirmVerified(board)
        assertNull(
            viewModel.board["a4"]
        )
        assertNull(
            viewModel.board["a3"]
        )
    }

    @Test
    fun `valid move`() = runTest(UnconfinedTestDispatcher()) {
        // GIVEN
        coEvery { board.setPosition(any()) } returns Unit
        coEvery { board.isMoveCorrect(any(), "g1f3") } returns true
        coEvery { board.move(any(), "g1f3") } returns Unit
        coEvery { board.getPosition() } returns POSITION_AFTER_1ST_MOVE_G1F3
        coEvery { board.getEvaluation(any()) } returns EVALUATION_50
        coEvery { engine1.getTopMoves(any(), any()) } returns emptyList()

        val viewModel = createViewModelAndCollectState()
        assertEquals(32, viewModel.board.value.pieces.size)
        assertNotNull(
            viewModel.board["g1"]
        )
        assertNull(
            viewModel.board["f3"]
        )

        // WHEN a valid move is clicked
        viewModel.onSquareClick("g1")
        viewModel.onSquareClick("f3")

        // THEN piece is moved
        assertNull(
            viewModel.board["g1"]
        )
        assertNotNull(
            viewModel.board["f3"]
        )

        // WHEN an empty square is clicked again
        clearMocks(board)
        viewModel.onSquareClick("a3")

        // THEN nothing happens
        confirmVerified(board)
        assertNull(
            viewModel.board["g1"]
        )
        assertNull(
            viewModel.board["a3"]
        )
        assertNotNull(
            viewModel.board["f3"]
        )
    }

    @Test
    fun `invalid move`() = runTest(UnconfinedTestDispatcher()) {
        // GIVEN
        coEvery { board.setPosition(any()) } returns Unit
        coEvery { board.isMoveCorrect(any(), "g1b5") } returns false

        val viewModel = createViewModelAndCollectState()
        assertNotNull(
            viewModel.board["g1"]
        )

        // WHEN
        viewModel.onSquareClick("g1")
        viewModel.onSquareClick("b5")

        // THEN
        coVerify { board.isMoveCorrect(POSITION_AT_START, "g1b5") }
        assertNotNull(
            viewModel.board["g1"]
        )
        assertNull(
            viewModel.board["b5"]
        )
    }

    @Test
    fun arrow() = runTest {
        // GIVEN
        coEvery { board.getPosition() } returns POSITION_AFTER_1ST_MOVE_G1F3
        coEvery { board.getEvaluation(any()) } returns EVALUATION_50
        coEvery { board.move(any(), any()) } just Runs

        coEvery { engine1.getTopMoves(any(), any()) } returns TOP_MOVES_AT_START

        val viewModel = createViewModelAndCollectState()
        viewModel.start()
        val expectedArrow = Arrow("g1", "f3", Arrow.COLOR_SUGGESTION)

        assertTrue(
            viewModel.board.value.arrows.contains(expectedArrow)
        )

        assertNotNull(
            viewModel.board[expectedArrow.start]
        )

        assertNull(
            viewModel.board[expectedArrow.end]
        )

        // WHEN
        viewModel.onSquareClick(expectedArrow.start)

        // THEN
        coVerify { board.move(any(), "g1f3") }
        assertNull(
            viewModel.board[expectedArrow.start]
        )

        assertNotNull(
            viewModel.board[expectedArrow.end]
        )
    }

    @Test
    fun `two arrows from the same square`() = runTest {
        // GIVEN
        coEvery { board.getPosition() } returns POSITION_AT_START
        coEvery { board.isMoveCorrect(any(), "d2d4") } returns true
        coEvery { board.getEvaluation(any()) } returns EVALUATION_50
        coEvery { board.move(any(), any()) } just Runs

        coEvery { engine1.getTopMoves(any(), any()) } returns TOP_MOVES_FROM_SAME_SQUARE

        val viewModel = createViewModelAndCollectState()

        viewModel.start()
        assertEquals(
            3,
            viewModel.board.value.arrows.size
        )

        // WHEN
        viewModel.onSquareClick("d2")

        // THEN two arrows are shown
        assertTrue(
            viewModel.board.value.arrows.all { it.start == "d2" }
        )
        assertEquals(
            2,
            viewModel.board.value.arrows.size
        )
        coVerify(exactly = 0) { board.move(any(), any()) }

        // WHEN 2nd square of the arrow is clicked
        viewModel.onSquareClick("d4")

        // THEN the move is made
        coVerify(exactly = 1) { board.move(any(), "d2d4") }
    }
}

private operator fun StateFlow<ChessBoardViewState>.get(key: SquareNotation): ChessBoardViewState.Piece? =
    value.pieces.firstOrNull { it.position == key }

private val TOP_MOVES_FROM_SAME_SQUARE = listOf(
    Engine.TopMove("engine-1", "d2d4", 29),
    Engine.TopMove("engine-1", "d2d3", 25),
    Engine.TopMove("engine-1", "e2e4", 23),
)
