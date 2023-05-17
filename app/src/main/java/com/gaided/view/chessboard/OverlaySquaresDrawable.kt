package com.gaided.view.chessboard

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import com.gaided.domain.SquareNotation

internal class OverlaySquaresDrawable(
    private val squares: Map<SquareNotation, ChessBoardView.Square>,
    private val overlaySquares: Set<ChessBoardView.State.OverlaySquare>
) : Drawable() {
    override fun draw(canvas: Canvas) {
        for(square in overlaySquares) {
            val overlaySquare = checkNotNull( squares[square.square])
            val overlayPaint = Paint().apply {
                color = square.color
                alpha = 128
            }

            overlaySquare.draw(canvas, overlayPaint)
        }
    }

    override fun setAlpha(alpha: Int) {}

    override fun setColorFilter(colorFilter: ColorFilter?) {}

    override fun getOpacity() = PixelFormat.OPAQUE
}