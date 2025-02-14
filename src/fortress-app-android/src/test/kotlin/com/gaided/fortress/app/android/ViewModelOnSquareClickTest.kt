package com.gaided.fortress.app.android

import com.gaided.chessui.model.ChessBoardViewState
import com.gaided.model.SquareNotation
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


internal class ViewModelOnSquareClickTest : FortressGameViewModelTestCase() {

    @Test
    fun onSquareClick() = runTest {
        // GIVEN
        val viewModel = createViewModelAndCollectState()

        // THEN board has pieces
        with(viewModel.chessBoardViewState.value) {
            assertEquals(32, pieces.size)
            assertFalse(pieces.find { it.position == "a2" }!!.isElevated)
        }

        // WHEN a2 is clicked
        viewModel.onSquareClick("a2")

        // THEN piece on a2 is elevated
        with(viewModel.chessBoardViewState.value) {
            assertTrue(pieces["a2"]!!.isElevated)
        }

        // WHEN b2 is clicked
        viewModel.onSquareClick("b2")

        // THEN both pieces are not elevated
        with(viewModel.chessBoardViewState.value) {
            assertFalse(pieces["a2"]!!.isElevated)
            assertFalse(pieces["b2"]!!.isElevated)
        }

        // WHEN b2 is clicked again
        viewModel.onSquareClick("b2")

        // THEN piece on b2 is elevated
        with(viewModel.chessBoardViewState.value) {
            assertTrue(pieces["b2"]!!.isElevated)
        }
    }
}

private operator fun Set<ChessBoardViewState.Piece>.get(squareNotation: SquareNotation): ChessBoardViewState.Piece? {
    return find { it.position == squareNotation }
}