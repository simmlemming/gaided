package gaided

import com.gaided.engine.Engine
import com.gaided.engine.Engine.TopMove
import com.gaided.engine.FenNotation
import com.gaided.engine.StockfishEngine
import com.gaided.engine.api.StockfishEngineApi
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
    fun `valid input`() = runTest {
        coEvery {
            api.getTopMoves(
                any(),
                any()
            )
        } returns "[{'Move': 'f1g2', 'Centipawn': -580, 'Mate': None}, {'Move': 'f1g1', 'Centipawn': -736, 'Mate': None}, {'Move': 'f1e1', 'Centipawn': -949, 'Mate': None}]"
        sut = StockfishEngine(api = api)

        assertEquals(
            listOf(
                TopMove(sut.name, "f1g2", -580),
                TopMove(sut.name, "f1g1", -736),
                TopMove(sut.name, "f1e1", -949)
            ),
            sut.getTopMoves()
        )
    }

    private suspend fun Engine.getTopMoves() = getTopMoves(FenNotation.START_POSITION, 3)
}