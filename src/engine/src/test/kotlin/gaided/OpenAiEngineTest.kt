package gaided

import com.gaided.engine.Engine
import com.gaided.engine.FenNotation
import com.gaided.engine.OpenAiEngine
import com.gaided.engine.RemoteBoard
import com.gaided.engine.RemoteBoard.TopMove
import com.gaided.engine.api.OpenAiEngineApi
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class OpenAiEngineTest {
    private lateinit var api: OpenAiEngineApi
    private lateinit var sut: OpenAiEngine

    @Before
    fun setUp() {
        api = mockk<OpenAiEngineApi>()
    }

    @Test
    fun `empty string`() = runTest {
        testEngine("", emptyList())
    }

    @Test
    fun `full notation`() = runTest {
        testEngine(
            "a2a4, g1f3, b7xb6",
            listOf(TopMove("a2a4"), TopMove("g1f3"), TopMove("b7b6"))
        )
    }

    @Test
    fun `full notation with dash`() = runTest {
        testEngine(
            "a2-a4",
            listOf(TopMove("a2a4"))
        )
    }

    @Test
    fun `invalid input`() = runTest {
        testEngine(
            "Nf3, invalid, ...d4",
            emptyList()
        )
    }

    private suspend fun testEngine(
        apiResponse: String,
        expected: List<TopMove>
    ) {
        every { api.getTopMoves(any(), any()) } returns apiResponse
        sut = OpenAiEngine(api)

        assertEquals(
            expected,
            sut.getTopMoves()
        )
    }

    private suspend fun Engine.getTopMoves() = getTopMoves(FenNotation.START_POSITION, 3)
}