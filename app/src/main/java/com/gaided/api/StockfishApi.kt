package com.gaided.api

import java.io.*
import java.net.HttpURLConnection
import java.net.URL

internal class StockfishApi(
    baseUrl: String
) {

    private val url = URL("$baseUrl/call")

    fun getFenPosition(): String =
        call("get_fen_position")

    private fun call(method: String, vararg params: Any): String {
        val requestBody = """
            {
                "method": "$method",
                "args": []
            }
            """.trimIndent()

        var connection: HttpURLConnection? = null
        try {
            connection = (url.openConnection() as HttpURLConnection).apply {
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Content-Length", requestBody.length.toString())
            }

            DataOutputStream(connection.outputStream).use {
                it.writeBytes(requestBody)
            }

            return connection.inputStream.readAdString()
        } catch (e: Exception) {
            val errorMessage = connection?.errorStream?.readAdString()
            throw IOException(errorMessage, e)
        } finally {
            connection?.disconnect()
        }
    }

    private fun InputStream.readAdString(): String {
        val result = StringBuilder()

        BufferedReader(InputStreamReader(this)).use {
            var line = it.readLine()
            while (line != null) {
                result.append(line)
                line = it.readLine()
            }
        }

        return result.toString()
    }
}
