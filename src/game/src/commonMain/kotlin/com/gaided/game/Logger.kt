package com.gaided.game

expect fun logd(tag: String, message: String)
expect fun logi(tag: String, message: String)
expect fun loge(tag: String, message: String, e: Throwable? = null)