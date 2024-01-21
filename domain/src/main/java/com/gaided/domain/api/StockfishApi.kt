@file:Suppress("RedundantVisibilityModifier")

package com.gaided.domain.api

import java.io.*
import java.net.HttpURLConnection
import java.net.URL

public open class StockfishApi(
    baseUrl: String,
    private val openConnection: ((URL) -> HttpURLConnection) = { url -> url.openConnection() as HttpURLConnection }
) {

    private val url = URL("$baseUrl/call")

    public fun getFenPosition(): String =
        call("get_fen_position")

    public fun setFenPosition(position: String) {
        call("set_fen_position", position)
    }

    public fun makeMovesFromCurrentPosition(moves: List<String>): String =
        call("make_moves_from_current_position", moves)

    public fun getTopMoves(numberOfMoves: Int): String =
        call("get_top_moves", numberOfMoves)

    public fun getEvaluation(): String =
        call("get_evaluation")

    protected open fun formatArgs(args: List<*>): String = args.joinToString {
        when (it) {
            is String -> "\"$it\""
            is List<*> -> "[${formatArgs(it)}]"
            else -> it.toString()
        }
    }

    private fun call(method: String, vararg args: Any): String {
        val requestBody = """
            {
                "method": "$method",
                "args": [${formatArgs(args.asList())}]
            }
            """.trimIndent()

        println(requestBody)

        var connection: HttpURLConnection? = null
        try {
            connection = openConnection(url).apply {
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
