package com.gaided.game

import android.util.Log

actual fun logd(message: String) {
    Log.d("Gaided", message)
}

actual fun logi(message: String) {
    Log.i("Gaided", message)
}

actual fun loge(message: String, e: Throwable?) {
    Log.e("Gaided", message, e)
}