package com.gaided

import com.gaided.domain.Engine
import com.gaided.domain.FenNotation
import com.gaided.domain.api.StockfishApi
import com.gaided.view.chessboard.ChessBoardView.State.Arrow
import com.gaided.view.evaluation.EvaluationView
import com.gaided.view.player.PlayerView
import com.gaided.view.player.PlayerView.State.Move
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class GameViewModelTest {
    private lateinit var api: StockfishApi
    private lateinit var viewModel: GameViewModel
    private lateinit var testDispatcher: TestDispatcher

    @Before
    fun setUp() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
    }

    @Test
    fun `initial state`() = runTest {
        api = mockk()
        viewModel = createViewModel(backgroundScope)

        advanceUntilIdle()
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
        api = mockk {
            every { setFenPosition(any()) } returns Unit
            every { getEvaluation() } returns "{\"type\": \"cp\", \"value\": 50}"
            every { getTopMoves(any()) } returns """
                [
                    {'Move': 'd2d4', 'Centipawn': 29, 'Mate': None},
                    {'Move': 'g1f3', 'Centipawn': 25, 'Mate': None},
                    {'Move': 'e2e4', 'Centipawn': 23, 'Mate': None}
                ]
            """.trimIndent()
        }
        viewModel = createViewModel(backgroundScope)

        viewModel.start()
        advanceUntilIdle()

        verify { api.setFenPosition(FenNotation.START_POSITION.fenString) }
        verify { api.getEvaluation() }
        verify { api.getTopMoves(3) }

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
            move1 = Move("d2d4", true, "d2d4", "piece_pw"),
            move2 = Move("g1f3", true, "g1f3", "piece_nw"),
            move3 = Move("e2e4", true, "e2e4", "piece_pw")
        )

        assertEquals(expectedPlayerWhiteState, viewModel.playerWhite.value)
        assertEquals(PlayerView.State.OPPONENT_MOVE, viewModel.playerBlack.value)
        assertEquals(EvaluationView.State(50, false), viewModel.evaluation.value)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(scope: CoroutineScope) = GameViewModel(Game(Engine(api, testDispatcher)))
        .also {
            scope.launch { it.board.collect() }
            scope.launch { it.evaluation.collect() }
            scope.launch { it.playerWhite.collect() }
            scope.launch { it.playerBlack.collect() }
        }
}