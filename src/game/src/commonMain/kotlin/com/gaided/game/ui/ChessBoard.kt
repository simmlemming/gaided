package com.gaided.game.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import com.gaided.engine.SquareNotation
import com.gaided.game.ui.model.ChessBoardViewState
import kotlinx.coroutines.flow.Flow

private val colorDarkSquare = Color(0xBA, 0x97, 0x72)
private val colorLightSquare = Color(0xF1, 0xDF, 0xC0)
private val colorBorder = Color(0xBA, 0xA7, 0x93)
private val colorBorderText = Color.White
private val borderTextStyle = TextStyle(color = colorBorderText)

private val DrawScope.borderSize
    get() = size.width / 24

@Composable
internal fun chessBoardView(boardState: Flow<ChessBoardViewState>, modifier: Modifier = Modifier) {
    val textMeasurer = rememberTextMeasurer()
    Box(modifier = Modifier
        .fillMaxSize()
        .aspectRatio(1f)
        .drawWithContent {
            drawSquares()
            drawBorder()
            drawRowNumbers(textMeasurer)
            drawColumnLetters(textMeasurer)
        }
    )
}

private val _squares = mutableMapOf<SquareNotation, Square>().also {
    for (row in 1..8) {
        for (column in 1..8) {
            val square = Square(row, column)
            it[square.notation] = square
        }
    }
}

private val squares = _squares.toMap()

private fun Square.toColor() = if ((row + column) % 2 == 0) {
    colorDarkSquare
} else {
    colorLightSquare
}


private fun DrawScope.drawSquares() {
    Square.sideLength = (this.size.width - borderSize * 2) / 8f
    Square.borderLength = borderSize

    for (square in squares.values) {
        square.draw(this, square.toColor())
    }
}

private fun DrawScope.drawBorder() {
    val width = size.width
    val height = size.height
    val borderSizePx = borderSize

    drawRect(
        colorBorder,
        topLeft = Offset.Zero,
        size = Size(width, borderSizePx)
    )
    drawRect(
        colorBorder,
        topLeft = Offset(0f, height - borderSizePx),
        size = Size(width, borderSizePx)
    )
    drawRect(
        colorBorder,
        topLeft = Offset.Zero,
        size = Size(borderSizePx, height)
    )
    drawRect(
        colorBorder,
        topLeft = Offset(width - borderSizePx, 0f),
        size = Size(borderSizePx, height)
    )
}

private fun DrawScope.drawRowNumbers(textMeasurer: TextMeasurer) {
    val width = size.width
    val textWidth = textMeasurer.measure("0").size.width
    val textHeight = textMeasurer.measure("0").size.height

    for (row in 1..8) {
        val text = row.toString()
        this.drawText(
            textMeasurer = textMeasurer,
            text = text,
            style = borderTextStyle,
            topLeft = Offset(width - (borderSize + textWidth) / 2, squares["a$row"]!!.center.y - textHeight/2),
        )
    }
}

private fun DrawScope.drawColumnLetters(textMeasurer: TextMeasurer) {
    val height = size.height
    val textWidth = textMeasurer.measure("0").size.width
    val textHeight = textMeasurer.measure("0").size.height

    for (column in 1..8) {
        val letter = COLUMN_LETTERS[column].toString()

        this.drawText(
            textMeasurer = textMeasurer,
            text = letter,
            style = borderTextStyle,
            topLeft = Offset(
                squares["${letter}1"]!!.center.x - textWidth / 2,
                height - (borderSize + textHeight) / 2
            ),
        )
    }
}

private data class Square(val row: Int, val column: Int) {
    val topLeftCorner: Offset
        get() = Offset(
            (column - 1) * sideLength + borderLength,
            (8 - row) * sideLength + borderLength
        )

    val center: Offset
        get() = Offset(topLeftCorner.x + sideLength / 2f, topLeftCorner.y + sideLength / 2f)

    val bottomRightCorner: Offset
        get() = Offset(
            topLeftCorner.x + sideLength,
            topLeftCorner.y + sideLength
        )

    val rect: RectF
        get() = RectF(
            topLeftCorner.x, topLeftCorner.y,
            bottomRightCorner.x, bottomRightCorner.y
        )

    val notation: String = "${COLUMN_LETTERS[column]}$row"

    fun draw(drawScope: DrawScope, color: Color) {
        drawScope.drawRect(
            color = color,
            topLeft = topLeftCorner,
            size = Size(width = sideLength, height = sideLength),
        )
    }

    companion object {
        var sideLength: Float = 0f
        var borderLength: Float = 0f
    }
}

private const val COLUMN_LETTERS = "_abcdefgh"
