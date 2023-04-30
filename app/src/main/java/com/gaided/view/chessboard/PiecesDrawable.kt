package com.gaided.view.chessboard

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable

internal class PiecesDrawable(
    private val squares: Map<SquareNotation, ChessBoardView.Square>,
    private val pieces: Set<ChessBoardView.State.Piece>
) : Drawable() {
    override fun draw(canvas: Canvas) {
        for (piece in pieces) {
            val center = squares[piece.position]!!.center
            val paint = Paint().apply { color = piece.color }
            canvas.drawCircle(center.x, center.y, 24f, paint)
        }
    }

    override fun setAlpha(alpha: Int) {}

    override fun setColorFilter(colorFilter: ColorFilter?) {}

    override fun getOpacity(): Int = PixelFormat.OPAQUE

    override fun equals(other: Any?) =
        this.pieces == (other as? PiecesDrawable)?.pieces

    override fun hashCode() = pieces.hashCode()
}