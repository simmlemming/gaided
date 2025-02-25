package com.gaided.fortress.app.android

import com.gaided.chessui.model.ChessBoardViewState
import com.gaided.model.SquareNotation
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue


internal class FortressViewModelTest : FortressViewModelTestCase() {

    @Before
    fun setUpFortressTest() {
        createPlayer = { _, _ -> TODO() }
    }

    @Test
    fun onSquareClick() = runTest {
        // GIVEN
        val viewModel = createViewModelAndCollectState()

        // THEN board has pieces
        with(viewModel.board.value) {
            assertEquals(32, pieces.size)
            assertFalse(pieces["a2"]!!.isElevated)
        }

        // WHEN a2 is clicked
        viewModel.onSquareClick("a2")

        // THEN piece on a2 is elevated
        assertTrue(
            viewModel.board["a2"]!!.isElevated
        )

        // WHEN a2 is clicked again
        viewModel.onSquareClick("a2")

        // THEN piece on b2 is elevated
        assertFalse(
            viewModel.board["a2"]!!.isElevated
        )
    }

    @Test
    fun correctMove() = runTest {
        // GIVEN
        coEvery { board.isMoveCorrect(any(), any()) } returns true
        coEvery { board.move(any(), any()) } just Runs

        val viewModel = createViewModelAndCollectState()
        viewModel.start()

        // WHEN move is made
        viewModel.onSquareClick("a2")
        viewModel.onSquareClick("a3")

        // THEN board is updated
        assertNull(viewModel.board["a2"])
        assertNotNull(viewModel.board["a3"])
    }

    @Test
    fun incorrectMove() = runTest {
        // GIVEN
        coEvery { board.isMoveCorrect(any(), any()) } returns false

        val viewModel = createViewModelAndCollectState()
        viewModel.start()

        // WHEN move is made
        viewModel.onSquareClick("a2")
        viewModel.onSquareClick("a3")

        // THEN board is not updated
        assertNotNull(viewModel.board["a2"])
        assertNull(viewModel.board["a3"])
    }
}

private operator fun StateFlow<ChessBoardViewState>.get(squareNotation: SquareNotation) = value[squareNotation]

private operator fun ChessBoardViewState.get(squareNotation: SquareNotation) = pieces[squareNotation]

private operator fun Set<ChessBoardViewState.Piece>.get(squareNotation: SquareNotation) = find {
    it.position == squareNotation
}
