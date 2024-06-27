package com.gaided.logger

actual fun logv(message: String) {
    log("VERBOSE", message, null)
}

actual fun logd(message: String) {
    log("DEBUG", message, null)
}

actual fun logi(message: String) {
    log("INFO", message, null)
}

actual fun logw(message: String) {
    log("WARNING", message, null)
}

actual fun loge(message: String, e: Throwable?) {
    log("ERROR", message, e)
}

private fun log(level: String, message: String, e: Throwable?) {
    println("$level $message")
    e?.let {
        println(it.toString())
    }
}