package com.gaided.network

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.net.HttpURLConnection
import java.net.URL

public open class StockfishApi protected constructor(
    url: String,
    openConnection: ((URL) -> HttpURLConnection) = { it.openConnection() as HttpURLConnection },
) : HttpApi(openConnection) {

    private val endpoint: URL = URL("$url/call")
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
            url = this@StockfishApi.endpoint
            headers["Content-Type"] = "application/json"
            body = requestBody
            parse = { it }
            asString = { "$this [$method]" }
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