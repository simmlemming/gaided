package com.gaided.game

import android.util.Log

actual fun logd(tag: String, message: String) {
    Log.d(tag, message)
}

actual fun logi(tag: String, message: String) {
    Log.i(tag, message)
}

actual fun loge(tag: String, message: String, e: Throwable?) {
    Log.e(tag, message, e)
}