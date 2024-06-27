package com.gaided.logger

import android.util.Log

actual fun logv(message: String) {
    Log.v("Gaided", message)
}

actual fun logd(message: String) {
    Log.d("Gaided", message)
}

actual fun logi(message: String) {
    Log.i("Gaided", message)
}

actual fun logw(message: String) {
    Log.w("Gaided", message)
}

actual fun loge(message: String, e: Throwable?) {
    Log.e("Gaided", message, e)
}