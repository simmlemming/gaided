package com.gaided.logger

object Logger {
    var enabled: Boolean = false

    fun v(message: String) {
        if (enabled) logv(message)
    }
    fun d(message: String) {
        if (enabled) logd(message)
    }
    fun i(message: String) {
        if (enabled) logi(message)
    }
    fun w(message: String) {
        if (enabled) logw(message)
    }
    fun e(message: String, e: Throwable?) {
        if (enabled) loge(message, e)
    }
}

expect fun logv(message: String)
expect fun logd(message: String)
expect fun logi(message: String)
expect fun logw(message: String)
expect fun loge(message: String, e: Throwable? = null)
