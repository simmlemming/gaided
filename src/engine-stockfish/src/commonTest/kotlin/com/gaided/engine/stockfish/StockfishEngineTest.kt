package com.gaided.engine.stockfish

import com.gaided.engine.Engine
import com.gaided.engine.Engine.TopMove
import com.gaided.model.FenNotation
import com.gaided.model.toMove
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class StockfishEngineTest {
    private lateinit var api: StockfishEngineApi
    private lateinit var sut: StockfishEngine

    @Before
    fun setUp() {
        api = mockk<StockfishEngineApi>()
    }

    @Test
    fun `input with centipawn evaluation`() = runTest {
        coEvery {
            api.getTopMoves(
                any(),
                any()
            )
        } returns "[{'Move': 'f1g2', 'Centipawn': -580, 'Mate': None}, {'Move': 'f1g1', 'Centipawn': -736, 'Mate': None}, {'Move': 'f1e1', 'Centipawn': -949, 'Mate': None}]"
        sut = StockfishEngine(api = api)

        assertEquals(
            listOf(
                TopMove(sut.name, "f1g2".toMove(), -580),
                TopMove(sut.name, "f1g1".toMove(), -736),
                TopMove(sut.name, "f1e1".toMove(), -949)
            ),
            sut.getTopMoves()
        )
    }

    @Test
    fun `input with mate`() = runTest {
        coEvery {
            api.getTopMoves(
                any(),
                any()
            )
        } returns "[{'Move': 'f1g2', 'Centipawn': None, 'Mate': 3}, {'Move': 'f1g1', 'Centipawn': None, 'Mate': -1}]"
        sut = StockfishEngine(api = api)

        assertEquals(
            listOf(
                TopMove(sut.name, "f1g2".toMove(), null, 3),
                TopMove(sut.name, "f1g1".toMove(), null, -1),
            ),
            sut.getTopMoves()
        )
    }

    private suspend fun Engine.getTopMoves() = getTopMoves(FenNotation.START_POSITION, 3)
}