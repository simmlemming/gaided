package gaided.api

import com.gaided.engine.api.StockfishApi
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection

@ExperimentalCoroutinesApi
internal class StockfishApiTest {

    private lateinit var api: ApiUnderTest

    @Before
    fun setUp() {
        api = ApiUnderTest(mockConnection())
    }

    @Test
    fun formatArguments() {
        val cases = mapOf(
            listOf<Int>() to "",
            listOf(1, 2, "a") to "1, 2, \"a\"",
            listOf(listOf("a2a4", "e7e6")) to "[\"a2a4\", \"e7e6\"]"
        )

        for (case in cases) {
            assertEquals(case.value, api.formatArgs(case.key))
        }
    }

    @Test
    fun `call with primitive arguments`() = runTest {
        api.setFenPosition("one argument")

        val expected = """
            {
                "method": "set_fen_position",
                "args": ["one argument"]
            }
        """.trimIndent()

        assertRequestBody(api.connection, expected)
    }

    @Test
    fun `call without arguments`() = runTest {
        api.getFenPosition()

        val expected = """
            {
                "method": "get_fen_position",
                "args": []
            }
        """.trimIndent()

        assertRequestBody(api.connection, expected)
    }

    @Test
    fun `calls should be POST`() = runTest {
        api.getFenPosition()

        verify {
            api.connection.doOutput = true
            api.connection.setRequestProperty("Content-Type", "application/json")
            api.connection.setRequestProperty("Content-Length", any())
        }
    }

    @Test
    fun `position is set once`() = runTest {
        api.getTopMoves("pos-1", 3)
        api.getEvaluation("pos-1")

        // Combined bodies of all requests,
        // "set_fen_position" occurs once.
        val expectedRequests = """
            {
                "method": "set_fen_position",
                "args": ["pos-1"]
            }{
                "method": "get_top_moves",
                "args": [3]
            }{
                "method": "get_evaluation",
                "args": []
            }
        """.trimIndent()

        assertRequestBody(api.connection, expectedRequests)
    }

    @Test
    fun `position is set if changed`() = runTest {
        api.getTopMoves("pos-1", 3)
        api.getEvaluation("pos-2")

        // Combined bodies of all requests,
        // "set_fen_position" occurs twice.
        val expectedRequests = """
            {
                "method": "set_fen_position",
                "args": ["pos-1"]
            }{
                "method": "get_top_moves",
                "args": [3]
            }{
                "method": "set_fen_position",
                "args": ["pos-2"]
            }{
                "method": "get_evaluation",
                "args": []
            }
        """.trimIndent()

        assertRequestBody(api.connection, expectedRequests)
    }

    private fun mockConnection() = mockk<HttpURLConnection>(relaxed = true) {
        every { inputStream } returns ByteArrayInputStream("123".toByteArray())
        every { outputStream } returns ByteArrayOutputStream()
    }

    private fun assertRequestBody(connection: HttpURLConnection, expected: String) {
        val requestBodyStream = connection.outputStream as ByteArrayOutputStream
        val actual = String(requestBodyStream.toByteArray())
        assertEquals(expected, actual)
    }
}

private class ApiUnderTest(val connection: HttpURLConnection) : StockfishApi("http://a.b", { connection }) {
    public override fun formatArgs(args: List<*>): String {
        return super.formatArgs(args)
    }
}