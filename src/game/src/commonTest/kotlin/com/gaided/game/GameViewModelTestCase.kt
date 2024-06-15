@file:Suppress("PropertyName")

package com.gaided.game

import com.gaided.engine.FenNotation
import com.gaided.engine.RemoteBoard
import com.gaided.engine.StockfishEngine
import com.gaided.engine.api.RemoteBoardApi
import com.gaided.engine.api.StockfishEngineApi
import com.gaided.game.ui.model.ChessBoardViewState
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
    protected lateinit var remoteBoardApi: RemoteBoardApi
    protected lateinit var stockfishEngineApi: StockfishEngineApi
    protected lateinit var viewModel: GameViewModel
    private lateinit var testDispatcher: TestDispatcher

    @Before
    fun setUp() {
        testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    protected fun createViewModel() = GameViewModel(
        Game(
            RemoteBoard(remoteBoardApi, testDispatcher),
            listOf(StockfishEngine(stockfishEngineApi, testDispatcher))
        )
    )

    protected fun TestScope.createViewModelAndCollectState() = createViewModel()
        .also {
            val unconfinedScope = backgroundScope + UnconfinedTestDispatcher()
            unconfinedScope.launch { it.board.collect() }
            unconfinedScope.launch { it.evaluation.collect() }
            unconfinedScope.launch { it.playerWhite.collect() }
            unconfinedScope.launch { it.playerBlack.collect() }
        }

    protected val TOP_MOVES_AT_START = """
                [
                    {'Move': 'd2d4', 'Centipawn': 29, 'Mate': None},
                    {'Move': 'g1f3', 'Centipawn': 25, 'Mate': None},
                    {'Move': 'e2e4', 'Centipawn': 23, 'Mate': None}
                ]
            """.trimIndent()

    protected val TOP_MOVES_AFTER_1ST_MOVE = """
                [
                    {'Move': 'e7e6', 'Centipawn': 23, 'Mate': None}
                ]
            """.trimIndent()

    protected val FEN_POSITION_AT_START =
        FenNotation.START_POSITION.fenString

    protected val FEN_POSITION_AFTER_1ST_MOVE_G1F3 =
        "rnbqkbnr/pppppppp/8/8/8/5N2/PPPPPPPP/RNBQKB1R b KQkq - 1 1"

    protected val EVALUATION_50 = "{\"type\": \"cp\", \"value\": 50}"
    protected val EVALUATION_150 = "{\"type\": \"cp\", \"value\": 150}"

    protected val PIECE_WHITE_KNIGHT_AT_G1 = ChessBoardViewState.Piece("piece_nw", "g1")
    protected val PIECE_WHITE_KNIGHT_AT_F3 = ChessBoardViewState.Piece("piece_nw", "f3")
}
