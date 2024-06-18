@file:Suppress("PropertyName")

package com.gaided.game

import com.gaided.engine.Engine
import com.gaided.engine.FenNotation
import com.gaided.engine.Board
import com.gaided.game.ui.model.ChessBoardViewState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before

@OptIn(ExperimentalCoroutinesApi::class)
internal abstract class GameViewModelTestCase {
    protected lateinit var board: Board
    protected lateinit var engine1: Engine
    protected lateinit var viewModel: GameViewModel
    private lateinit var testDispatcher: TestDispatcher

    @Before
    fun setUp() {
        testDispatcher = UnconfinedTestDispatcher()
        board = mockk()
        engine1 = mockk {
            every { name } returns "engine-1"
            every { recommendedNumberOfMoves } returns 3
        }
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = GameViewModel(
        Game(board, listOf(engine1))
    )

    protected fun TestScope.createViewModelAndCollectState() = createViewModel()
        .also {
            val unconfinedScope = backgroundScope + UnconfinedTestDispatcher()
            unconfinedScope.launch { it.board.collect() }
            unconfinedScope.launch { it.evaluation.collect() }
            unconfinedScope.launch { it.playerWhite.collect() }
            unconfinedScope.launch { it.playerBlack.collect() }
        }

    protected val TOP_MOVES_AT_START = listOf(
        Engine.TopMove("engine-1", "d2d4", 29),
        Engine.TopMove("engine-1", "g1f3", 25),
        Engine.TopMove("engine-1", "e2e4", 23),
    )

    protected val TOP_MOVES_AFTER_1ST_MOVE = listOf(
        Engine.TopMove("engine-1", "e7e6", 23),
    )

    protected val POSITION_AT_START =
        FenNotation.START_POSITION

    protected val POSITION_AFTER_1ST_MOVE_G1F3 =
        FenNotation.fromFenString("rnbqkbnr/pppppppp/8/8/8/5N2/PPPPPPPP/RNBQKB1R b KQkq - 1 1")

    protected val EVALUATION_50 = Board.Evaluation("cp", 50)
    protected val EVALUATION_150 = Board.Evaluation("cp", 150)

    protected val PIECE_WHITE_KNIGHT_AT_G1 = ChessBoardViewState.Piece("piece_nw", "g1")
    protected val PIECE_WHITE_KNIGHT_AT_F3 = ChessBoardViewState.Piece("piece_nw", "f3")
}
