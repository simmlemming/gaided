package com.gaided.engine.openai

import com.gaided.engine.Engine
import com.gaided.model.FenNotation
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class OpenAiEngineTest {
    private lateinit var api: OpenAiEngineApi
    private lateinit var sut: OpenAiEngine

    @Before
    fun setUp() {
        api = mockk<OpenAiEngineApi>()
        sut = OpenAiEngine(api = api)
    }

    // TODO: O-O, Bb4+, Bxb8+, Bxb8, Queen a4 to f4, Bishop takes d5,
    //  1. e4, 1. d4, 1. Nf3, 1. ... Ng8f6, 1. ... Ng8h6, 1. ... Nb8c6

    @Test
    fun `empty string`() = runTest {
        testEngine(FEN_POSITION_START, "", emptyList())
    }

    @Test
    fun `short notation white rook takes`() = runTest {
        testEngine(
            FEN_POSITION_START,
            "Rxa5",
            listOf(Engine.TopMove(sut.name, "a1a5"))
        )
    }

    @Test
    fun `short notation black rook takes`() = runTest {
        testEngine(
            FEN_POSITION_AFTER_1ST_MOVE_G1F3,
            "Rxa5",
            listOf(Engine.TopMove(sut.name, "a8a5"))
        )
    }

    @Test
    fun `short notation black rook moves`() = runTest {
        testEngine(
            FEN_POSITION_AFTER_1ST_MOVE_G1F3,
            "Ra5, Rg8",
            listOf(Engine.TopMove(sut.name, "a8a5"), Engine.TopMove(sut.name, "a8g8"))
        )
    }

    @Test
    fun `short notation white rook moves`() = runTest {
        testEngine(
            FEN_POSITION_START,
            "Ra5, Rg1",
            listOf(Engine.TopMove(sut.name, "a1a5"), Engine.TopMove(sut.name, "a1g1"))
        )
    }

    @Test
    fun `short notation white rook moves with check`() = runTest {
        testEngine(
            FEN_POSITION_START,
            "Ra5+, Rg1+",
            listOf(Engine.TopMove(sut.name, "a1a5"), Engine.TopMove(sut.name, "a1g1"))
        )
    }

    @Test
    fun `short notation white pawn takes`() = runTest {
        testEngine(
            FEN_POSITION_START,
            "cxb3",
            listOf(Engine.TopMove(sut.name, "c2b3"))
        )
    }

    @Test
    fun `short notation black pawn takes`() = runTest {
        testEngine(
            FEN_POSITION_AFTER_1ST_MOVE_G1F3,
            "dxe6",
            listOf(Engine.TopMove(sut.name, "d7e6"))
        )
    }

    @Test
    fun `short notation white pawn moves`() = runTest {
        testEngine(
            FEN_POSITION_START,
            "a3, b4",
            listOf(Engine.TopMove(sut.name, "a2a3"), Engine.TopMove(sut.name, "b2b4"))
        )
    }

    @Test
    fun `short notation black pawn moves`() = runTest {
        testEngine(
            FEN_POSITION_AFTER_1ST_MOVE_G1F3,
            "a6, c5",
            listOf(Engine.TopMove(sut.name, "a7a6"), Engine.TopMove(sut.name, "c7c5"))
        )
    }

    @Test
    fun `full notation`() = runTest {
        testEngine(
            FEN_POSITION_START,
            "a2a4, g1f3, b7xb6",
            listOf(Engine.TopMove(sut.name, "a2a4"), Engine.TopMove(sut.name, "g1f3"), Engine.TopMove(sut.name, "b7b6"))
        )
    }

    @Test
    fun `full notation with dash`() = runTest {
        testEngine(
            FEN_POSITION_START,
            "a2-a4, Qd1-d2",
            listOf(Engine.TopMove(sut.name, "a2a4"), Engine.TopMove(sut.name, "d1d2"))
        )
    }

    @Test
    fun `full notation with to`() = runTest {
        testEngine(
            FEN_POSITION_START,
            "a2 to a4",
            listOf(Engine.TopMove(sut.name, "a2a4"))
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
        expected: List<Engine.TopMove>
    ) {
        every { api.getTopMoves(any(), any()) } returns apiResponse

        Assert.assertEquals(
            expected,
            sut.getTopMoves(position, 3)
        )
    }
}

private val FEN_POSITION_START = FenNotation.START_POSITION

private val FEN_POSITION_AFTER_1ST_MOVE_G1F3 = FenNotation.fromFenString(
    "rnbqkbnr/pppppppp/8/8/8/5N2/PPPPPPPP/RNBQKB1R b KQkq - 1 1"
)
