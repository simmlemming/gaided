package com.gaided.engine.api

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.net.HttpURLConnection
import java.net.URL

public sealed class StockfishApi protected constructor(
    baseUrl: String,
    openConnection: ((URL) -> HttpURLConnection) = { url -> url.openConnection() as HttpURLConnection }
) : HttpApi(openConnection) {

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

    protected fun call(method: String, vararg args: Any): String {
        val requestBody = """
            {
                "method": "$method",
                "args": [${formatArgs(args.asList())}]
            }
            """.trimIndent()

        return post {
            url = this@StockfishApi.url
            headers["Content-Type"] = "application/json"
            body = requestBody
            parse = { it }
        }
    }

    protected open fun formatArgs(args: List<*>): String = args.joinToString {
        when (it) {
            is String -> "\"$it\""
            is List<*> -> "[${formatArgs(it)}]"
            else -> it.toString()
        }
    }
}