package com.gaided

import com.gaided.domain.Engine
import com.gaided.domain.FenNotation
import com.gaided.domain.api.StockfishApi
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
    fun start() = runTest {
        api = mockk {
            every { setFenPosition(any()) } returns Unit
            every { getEvaluation() } returns "{\"type\": \"cp\", \"value\": 0}"
            every { getTopMoves(any()) } returns "[]"
        }
        viewModel = createViewModel()
        backgroundScope.launch { viewModel.collectState() }

        viewModel.start()
        advanceUntilIdle()

        with(viewModel.board.value) {
            assertEquals(32, pieces.size)
            assertTrue(arrows.isEmpty())
            assertTrue(overlaySquares.isEmpty())
        }

        verify { api.setFenPosition(FenNotation.START_POSITION.fenString) }
        verify { api.getEvaluation() }
        verify { api.getTopMoves(3) }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = GameViewModel(Game(Engine(api, testDispatcher)))

    private suspend fun GameViewModel.collectState() {
        board.collect()
    }
}