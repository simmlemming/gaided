package com.gaided.game

import com.gaided.engine.Engine
import com.gaided.engine.FenNotation
import com.gaided.game.ui.model.ChessBoardViewState.Arrow
import io.mockk.coEvery
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

internal class GameViewModelTopMovesTest : GameViewModelTestCase() {
    @Test
    fun `top moves arrows`() = runTest {
        // GIVEN
        var position = POSITION_AT_START

        coEvery { board.move(any(), any()) } returns Unit
        coEvery { board.isMoveCorrect(any(), any()) } returns true
        coEvery { board.getEvaluation(any()) } returns EVALUATION_50
        coEvery { board.getPosition() } answers { position }

        coEvery { engine1.getTopMoves(POSITION_AT_START, any()) } returns TOP_MOVES_AT_START
        coEvery { engine1.getTopMoves(POSITION_AFTER_1ST_MOVE_G1F3, any()) } returns TOP_3_MOVES_AFTER_G1F3
        coEvery { engine1.getTopMoves(POSITION_AFTER_1ST_BLACK_MOVE_B7B6, any()) } returns TOP_3_MOVES_AFTER_B7B6

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
        position = POSITION_AFTER_1ST_MOVE_G1F3
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
        position = POSITION_AFTER_1ST_BLACK_MOVE_B7B6
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

private val TOP_3_MOVES_AFTER_G1F3 = listOf(
    Engine.TopMove("engine-1", "e7e6", 23),
    Engine.TopMove("engine-1", "e7e5", -23),
    Engine.TopMove("engine-1", "b7b6", 7),
)

private val TOP_3_MOVES_AFTER_B7B6 = listOf(
    Engine.TopMove("engine-1", "b1c3", 55),
    Engine.TopMove("engine-1", "e2e4", 47),
    Engine.TopMove("engine-1", "d2d4", 37),
)

private val POSITION_AFTER_1ST_BLACK_MOVE_B7B6 =
    FenNotation.fromFenString("rnbqkbnr/p1pppppp/1p6/8/8/5N2/PPPPPPPP/RNBQKB1R w KQkq - 0 2")
