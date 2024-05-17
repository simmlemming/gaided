package com.gaided.game

actual fun logd(tag: String, message: String) {
    log("DEBUG", tag, message, null)
}

actual fun logi(tag: String, message: String) {
    log("INFO", tag, message, null)
}

actual fun loge(tag: String, message: String, e: Throwable?) {
    log("ERROR", tag, message, e)
}

private fun log(level: String, tag: String, message: String, e: Throwable?) {
    println("$tag:$level $message")
    e?.let {
        println(it.toString())
    }
}