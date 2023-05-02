package gaided.api

import com.gaided.domain.api.StockfishApi
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection

internal class StockfishApiTest {

    private lateinit var api: ApiUnderTest

    @Before
    fun setUp() {
        api = ApiUnderTest(mockConnection())
    }

    @Test
    fun `should call with correct primitive arguments`() {
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
    fun `calls should be POST`() {
        api.getFenPosition()

        verify {
            api.connection.doOutput = true
        }
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

private class ApiUnderTest(val connection: HttpURLConnection) :
    StockfishApi("http://a.b", { connection })