package com.gaided.fortress.game

import com.gaided.model.FenNotation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class FortressGame {
    private val _position = MutableStateFlow(FenNotation.START_POSITION)
    val position: SharedFlow<FenNotation> = _position.asSharedFlow()
}