package com.gaided.game

expect fun logd(message: String)
expect fun logi(message: String)
expect fun loge(message: String, e: Throwable? = null)