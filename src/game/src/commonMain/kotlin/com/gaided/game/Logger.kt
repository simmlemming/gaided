package com.gaided.game

import com.gaided.engine.Logger

object Logger : Logger {
    override fun v(message: String) = logv(message)

    override fun d(message: String) = logd(message)

    override fun i(message: String) = logi(message)

    override fun w(message: String) = logw(message)

    override fun e(message: String, e: Throwable?) = loge(message, e)

}

expect fun logv(message: String)
expect fun logd(message: String)
expect fun logi(message: String)
expect fun logw(message: String)
expect fun loge(message: String, e: Throwable? = null)
