package com.gaided.view.chessboard

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.drawable.Drawable
import com.gaided.domain.SquareNotation
import com.gaided.util.getDrawable

internal class PiecesDrawable(
    private val context: Context,
    private val squares: Map<SquareNotation, ChessBoardView.Square>,
    private val pieces: Set<ChessBoardView.State.Piece>
) : Drawable() {
    override fun draw(canvas: Canvas) {
        for (piece in pieces) {
            val pieceDrawable = context.getDrawable(piece.drawableName)
            pieceDrawable.bindToSquare(checkNotNull(squares[piece.position]))
            if (piece.isElevated) {
                // Scale it a bit
                pieceDrawable.bounds.inset(-20, -20)
//                val shadowDrawable = context.getShadowDrawable(piece.drawableName)
//                shadowDrawable.bindToSquare(checkNotNull(squares[piece.position]))
//                shadowDrawable.bounds.inset(-60, -60)
//                shadowDrawable.draw(canvas)
            }
            pieceDrawable.draw(canvas)
        }
    }

    override fun setAlpha(alpha: Int) {}

    override fun setColorFilter(colorFilter: ColorFilter?) {}

    override fun getOpacity(): Int = PixelFormat.OPAQUE

    override fun equals(other: Any?) =
        this.pieces == (other as? PiecesDrawable)?.pieces

    override fun hashCode() = pieces.hashCode()
}

private fun Drawable.bindToSquare(square: ChessBoardView.Square) {
    val topLeftCorner = square.topLeftCorner
    val bottomRightCorner = square.bottomRightCorner
    this.bounds = Rect(
        topLeftCorner.x.toInt(), topLeftCorner.y.toInt(),
        bottomRightCorner.x.toInt(), bottomRightCorner.y.toInt()
    )
}

private fun Context.getShadowDrawable(name: String) = getDrawable(name).apply {
    setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY)
    alpha = 50
}
