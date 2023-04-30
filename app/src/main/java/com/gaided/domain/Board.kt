package com.gaided.domain

import com.gaided.FenConverter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal typealias Fen = String

internal class Board {
    private val _position = MutableStateFlow<Map<Square, Piece>>(emptyMap())
    val position = _position.asStateFlow()

    private val fenConverter = FenConverter()

    fun setPosition(position: Fen) {
        _position.value = fenConverter.fromFen(position)
    }

    internal sealed class Piece

    internal data class Square(val rank: Int, val file: Char)
}
