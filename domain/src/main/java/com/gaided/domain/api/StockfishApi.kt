@file:Suppress("RedundantVisibilityModifier")

package com.gaided.domain.api

import com.gaided.domain.MoveNotation
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

public open class StockfishApi(
    baseUrl: String,
    private val openConnection: ((URL) -> HttpURLConnection) = { url -> url.openConnection() as HttpURLConnection }
) {

    private val url = URL("$baseUrl/call")

    private var lastSetPosition: String? = null
    private val mutex = Mutex()

    public suspend fun getFenPosition(): String = mutex.withLock {
        call("get_fen_position")
    }

    public suspend fun setFenPosition(position: String): Unit = withPosition(position) {

    }

    public suspend fun makeMoves(position: String, moves: List<String>): Unit = withPosition(position) {
        call("make_moves_from_current_position", moves)
    }

    public suspend fun getTopMoves(position: String, numberOfMoves: Int): String = withPosition(position) {
        call("get_top_moves", numberOfMoves)
    }

    public suspend fun getEvaluation(position: String): String = withPosition(position) {
        call("get_evaluation")
    }

    public suspend fun isMoveCorrect(position: String, move: MoveNotation): Boolean = withPosition(position) {
        val response = call("is_move_correct", move)
        response == "True"
    }

    private suspend fun <R> withPosition(position: String, block: () -> R): R = mutex.withLock {
        if (lastSetPosition != position) {
            call("set_fen_position", position)
            lastSetPosition = position
        }

        return block()
    }

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
