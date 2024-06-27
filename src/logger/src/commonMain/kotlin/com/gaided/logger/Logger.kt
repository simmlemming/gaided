package com.gaided.logger

object Logger {
    fun v(message: String) = logv(message)
    fun d(message: String) = logd(message)
    fun i(message: String) = logi(message)
    fun w(message: String) = logw(message)
    fun e(message: String, e: Throwable?) = loge(message, e)
}

expect fun logv(message: String)
expect fun logd(message: String)
expect fun logi(message: String)
expect fun logw(message: String)
expect fun loge(message: String, e: Throwable? = null)
