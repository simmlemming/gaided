package com.gaided.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import com.gaided.domain.PieceNotation
import com.gaided.domain.SquareNotation
import com.gaided.view.chessboard.ChessBoardView


internal fun Map.Entry<SquareNotation, PieceNotation>.toPiece() = ChessBoardView.State.Piece(
    drawableName = value.toDrawableName(),
    position = key,
)

private fun PieceNotation.toDrawableName(): String {
    val color = if (this.isLowerCase()) "b" else "w"
    val symbol = this.lowercaseChar()
    return "piece_$symbol$color"
}

@SuppressLint("DiscouragedApi")
internal fun Context.getDrawable(name: String): Drawable {
    val id = resources.getIdentifier(name, "drawable", packageName)
    val drawable = AppCompatResources.getDrawable(this, id)

    return checkNotNull(drawable)
}

