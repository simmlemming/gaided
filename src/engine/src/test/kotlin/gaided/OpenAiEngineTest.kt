package gaided

import com.gaided.engine.Engine
import com.gaided.engine.Engine.TopMove
import com.gaided.engine.FenNotation
import com.gaided.engine.OpenAiEngine
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
        sut = OpenAiEngine(api)
    }

    // TODO: Re8, dxc5, O-O, Bb4+, Bxb8

    @Test
    fun `empty string`() = runTest {
        testEngine(FEN_POSITION_START, "", emptyList())
    }

    @Test
    fun `short notation pawn black`() = runTest {
        testEngine(
            FEN_POSITION_AFTER_1ST_MOVE_G1F3,
            "a6, c5",
            listOf(TopMove(sut.name, "a7a6"), TopMove(sut.name, "c7c5"))
        )
    }

    @Test
    fun `short notation pawn white`() = runTest {
        testEngine(
            FEN_POSITION_START,
            "a3, b4",
            listOf(TopMove(sut.name, "a2a3"), TopMove(sut.name, "b2b4"))
        )
    }

    @Test
    fun `full notation`() = runTest {
        testEngine(
            FEN_POSITION_START,
            "a2a4, g1f3, b7xb6",
            listOf(TopMove(sut.name, "a2a4"), TopMove(sut.name, "g1f3"), TopMove(sut.name, "b7b6"))
        )
    }

    @Test
    fun `full notation with dash`() = runTest {
        testEngine(
            FEN_POSITION_START,
            "a2-a4, Qd1-d2",
            listOf(TopMove(sut.name, "a2a4"), TopMove(sut.name, "d1d2"))
        )
    }

    @Test
    fun `full notation with to`() = runTest {
        testEngine(
            FEN_POSITION_START,
            "a2 to a4",
            listOf(TopMove(sut.name, "a2a4"))
        )
    }

    @Test
    fun `invalid input`() = runTest {
        testEngine(
            FEN_POSITION_START,
            "Nf3, invalid, ...d4",
            emptyList()
        )
    }

    private suspend fun testEngine(
        position: FenNotation,
        apiResponse: String,
        expected: List<TopMove>
    ) {
        every { api.getTopMoves(any(), any()) } returns apiResponse

        assertEquals(
            expected,
            sut.getTopMoves(position)
        )
    }

    private suspend fun Engine.getTopMoves(position: FenNotation) = getTopMoves(position, 3)
}

private val FEN_POSITION_START = FenNotation.START_POSITION

private val FEN_POSITION_AFTER_1ST_MOVE_G1F3 = FenNotation.fromFenString(
    "rnbqkbnr/pppppppp/8/8/8/5N2/PPPPPPPP/RNBQKB1R b KQkq - 1 1"
)
