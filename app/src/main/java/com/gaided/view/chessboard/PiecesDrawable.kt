package com.gaided.view.chessboard

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.appcompat.content.res.AppCompatResources
import com.gaided.domain.SquareNotation

internal class PiecesDrawable(
    private val context: Context,
    private val squares: Map<SquareNotation, ChessBoardView.Square>,
    private val pieces: Set<ChessBoardView.State.Piece>
) : Drawable() {
    override fun draw(canvas: Canvas) {
        for (piece in pieces) {
            val pieceDrawable = context.getDrawable(piece.drawableName)
            pieceDrawable.bindToSquare(checkNotNull(squares[piece.position]))
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

@SuppressLint("DiscouragedApi")
private fun Context.getDrawable(name: String): Drawable {
    val id = resources.getIdentifier(name, "drawable", packageName)
    val drawable = AppCompatResources.getDrawable(this, id)

    return checkNotNull(drawable)
}

private fun Drawable.bindToSquare(square: ChessBoardView.Square) {
    val topLeftCorner = square.topLeftCorner
    val bottomRightCorner = square.bottomRightCorner
    this.bounds = Rect(
        topLeftCorner.x.toInt(), topLeftCorner.y.toInt(),
        bottomRightCorner.x.toInt(), bottomRightCorner.y.toInt()
    )
}
