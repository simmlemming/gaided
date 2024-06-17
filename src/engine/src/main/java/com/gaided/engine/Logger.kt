package com.gaided.engine

public interface Logger {
    public fun v(message: String)
    public fun d(message: String)
    public fun i(message: String)
    public fun w(message: String)
    public fun e(message: String, e: Throwable?)
}

internal object DefaultLogger : Logger {
    override fun v(message: String) = log("VERBOSE", message)

    override fun d(message: String) = log("DEBUG", message)

    override fun i(message: String) = log("INFO", message)

    override fun w(message: String) = log("WARNING", message)

    override fun e(message: String, e: Throwable?) = log("ERROR", message, e)

    private fun log(level: String, message: String, e: Throwable? = null) {
        println("$level $message")
        e?.let {
            println(it.toString())
        }
    }
}