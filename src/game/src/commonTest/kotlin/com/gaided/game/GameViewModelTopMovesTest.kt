package com.gaided.game

import com.gaided.game.ui.model.ChessBoardViewState.Arrow
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

internal class GameViewModelTopMovesTest : GameViewModelTestCase() {
    @Test
    fun `top moves arrows`() = runTest {
        // GIVEN
        var apiPosition = FEN_POSITION_AT_START

        remoteBoardApi = mockk {
            coEvery { makeMoves(any(), any()) } returns Unit
            coEvery { isMoveCorrect(any(), any()) } returns true
            coEvery { getEvaluation(any()) } returns EVALUATION_50
            coEvery { getFenPosition() } answers { apiPosition }
        }

        stockfishEngineApi = mockk {
            coEvery { getTopMoves(FEN_POSITION_AT_START, 3) } returns TOP_MOVES_AT_START
            coEvery { getTopMoves(FEN_POSITION_AFTER_1ST_MOVE_G1F3, 3) } returns TOP_3_MOVES_AFTER_G1F3
            coEvery { getTopMoves(FEN_POSITION_AFTER_1ST_BLACK_MOVE_B7B6, 3) } returns TOP_3_MOVES_AFTER_B7B6
        }

        val viewModel = createViewModelAndCollectState()

        // WHEN game starts
        viewModel.start()

        // THEN top moves are filled
        assertEquals(
            setOf(
                Arrow("d2", "d4", Arrow.COLOR_SUGGESTION),
                Arrow("g1", "f3", Arrow.COLOR_SUGGESTION),
                Arrow("e2", "e4", Arrow.COLOR_SUGGESTION)
            ),
            viewModel.board.value.arrows
        )

        // WHEN white moves
        apiPosition = FEN_POSITION_AFTER_1ST_MOVE_G1F3
        viewModel.onSquareClick("g1")

        // THEN top moves include new response and old moves colored by evaluation
        assertEquals(
            setOf(
                // New top moves
                Arrow("e7", "e6", Arrow.COLOR_SUGGESTION),
                Arrow("e7", "e5", Arrow.COLOR_SUGGESTION),
                Arrow("b7", "b6", Arrow.COLOR_SUGGESTION),
            ) + setOf( // Old top moves colored according to evaluation
                Arrow("d2", "d4", Arrow.colorByTopMoveIndex(0)),
                Arrow("g1", "f3", Arrow.colorByTopMoveIndex(1)),
                Arrow("e2", "e4", Arrow.colorByTopMoveIndex(2))
            ),
            viewModel.board.value.arrows
        )

        // WHEN black moves
        apiPosition = FEN_POSITION_AFTER_1ST_BLACK_MOVE_B7B6
        viewModel.onSquareClick("b7")

        // THEN top moves include new response and old moves colored by evaluation
        assertEquals(
            setOf(
                // New top moves
                Arrow("b1", "c3", Arrow.COLOR_SUGGESTION),
                Arrow("e2", "e4", Arrow.COLOR_SUGGESTION),
                Arrow("d2", "d4", Arrow.COLOR_SUGGESTION),
            ) + setOf(
                // Old top moves colored according to evaluation
                Arrow("e7", "e6", Arrow.colorByTopMoveIndex(2)),
                Arrow("e7", "e5", Arrow.colorByTopMoveIndex(0)),
                Arrow("b7", "b6", Arrow.colorByTopMoveIndex(1)),
            ),
            viewModel.board.value.arrows
        )
    }
}

private val TOP_3_MOVES_AFTER_G1F3 = """
    [
        {'Move': 'e7e6', 'Centipawn': 23, 'Mate': None},
        {'Move': 'e7e5', 'Centipawn': -23, 'Mate': None},
        {'Move': 'b7b6', 'Centipawn': 7, 'Mate': None}
    ]
""".trimIndent()

private val TOP_3_MOVES_AFTER_B7B6 = """
    [
        {'Move': 'b1c3', 'Centipawn': 55, 'Mate': None},
        {'Move': 'e2e4', 'Centipawn': 47, 'Mate': None},
        {'Move': 'd2d4', 'Centipawn': 37, 'Mate': None}
    ]
""".trimIndent()

private const val FEN_POSITION_AFTER_1ST_BLACK_MOVE_B7B6 =
    "rnbqkbnr/p1pppppp/1p6/8/8/5N2/PPPPPPPP/RNBQKB1R w KQkq - 0 2"