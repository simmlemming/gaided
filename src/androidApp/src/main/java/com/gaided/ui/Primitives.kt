package com.gaided.ui

internal data class RectF(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    fun contains(x: Float, y: Float) =
        left < x && x < right && top < y && y < bottom
}