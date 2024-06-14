package com.gaided.engine.api

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

public sealed class StockfishApi protected constructor(
    baseUrl: String,
    openConnection: ((URL) -> HttpURLConnection) = { url -> url.openConnection() as HttpURLConnection }
) : HttpApi(baseUrl, openConnection) {

    private val url: URL = URL("$baseUrl/call")
    private var lastSetPosition: String? = null
    protected val mutex: Mutex = Mutex()

    protected suspend fun <R> withPosition(position: String, block: () -> R): R = mutex.withLock {
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

    protected fun call(method: String, vararg args: Any): String {
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