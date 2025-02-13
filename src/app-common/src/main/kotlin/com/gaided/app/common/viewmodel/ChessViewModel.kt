package com.gaided.app.common.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gaided.logger.Logger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

open class ChessViewModel : ViewModel() {
    private val exceptionsHandler = CoroutineExceptionHandler { _, e ->
        Logger.e("", e)
        _userMessage.value = e.message ?: "Error"
    }

    protected val safeViewModelScope: CoroutineScope = viewModelScope + exceptionsHandler

    private val _userMessage = MutableStateFlow("")
    val userMessage = _userMessage.asStateFlow()

    fun onUserMessageShown() {
        _userMessage.value = ""
    }

    protected fun launch(block: suspend CoroutineScope.() -> Unit) =
        safeViewModelScope.launch(block = block)

    protected fun <T> Flow<T>.stateInThis(
        initialValue: T,
        started: SharingStarted = SharingStarted.WhileSubscribed(5000)
    ): StateFlow<T> = stateIn(safeViewModelScope, started, initialValue)
}