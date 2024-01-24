package com.gaided

import com.gaided.domain.SquareNotation
import com.gaided.view.chessboard.ChessBoardView
import com.gaided.view.chessboard.ChessBoardView.State.Arrow
import io.mockk.Runs
import io.mockk.clearMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
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
        api = mockk()
        val viewModel = createViewModel()
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
        confirmVerified(api)
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
        api = mockk {
            every { setFenPosition(any()) } returns Unit
            every { isMoveCorrect("g1f3") } returns true
            every { makeMovesFromCurrentPosition(listOf("g1f3")) } returns Unit
            every { getFenPosition() } returns FEN_POSITION_AFTER_1ST_MOVE_G1F3
            every { getEvaluation() } returns "{}"
            every { getTopMoves(any()) } returns "[]"
        }

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
        clearMocks(api)
        viewModel.onSquareClick("a3")

        // THEN nothing happens
        confirmVerified(api)
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
        api = mockk {
            every { setFenPosition(any()) } returns Unit
            every { isMoveCorrect("g1b5") } returns false
        }

        val viewModel = createViewModelAndCollectState()
        assertNotNull(
            viewModel.board["g1"]
        )

        // WHEN
        viewModel.onSquareClick("g1")
        viewModel.onSquareClick("b5")

        // THEN
        verify { api.setFenPosition(any()) }
        verify { api.isMoveCorrect("g1b5") }
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
        api = mockk(relaxed = true) {
            every { getFenPosition() } returns FEN_POSITION_AFTER_1ST_MOVE_G1F3
            every { getEvaluation() } returns EVALUATION_50
            every { getTopMoves(any()) } returns TOP_MOVES_AT_START
            every { makeMovesFromCurrentPosition(any()) } just Runs
        }

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
        verify { api.makeMovesFromCurrentPosition(listOf("g1f3")) }
        assertNull(
            viewModel.board[expectedArrow.start]
        )

        assertNotNull(
            viewModel.board[expectedArrow.end]
        )
    }
}

private operator fun StateFlow<ChessBoardView.State>.get(key: SquareNotation): ChessBoardView.State.Piece? =
    value.pieces.firstOrNull { it.position == key }
