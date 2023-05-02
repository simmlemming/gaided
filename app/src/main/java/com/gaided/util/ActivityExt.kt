package com.gaided

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal suspend fun AppCompatActivity.showMessageIfNotEmpty(message: String, onShown: () -> Unit) {
    if (message.isEmpty()) {
        return
    }

    Toast
        .makeText(this, message, Toast.LENGTH_SHORT)
        .show()

    delay(500)
    onShown()
}

internal fun AppCompatActivity.repeatOnResumed(block: suspend CoroutineScope.() -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.RESUMED, block)
    }
}