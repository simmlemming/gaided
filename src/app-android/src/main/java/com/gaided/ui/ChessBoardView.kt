package com.gaided.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import com.gaided.R
import com.gaided.game.ui.model.ChessBoardViewState
import com.gaided.model.SquareNotation
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

private val colorDarkSquare = Color(0xBA, 0x97, 0x72)
private val colorLightSquare = Color(0xF1, 0xDF, 0xC0)
private val colorBorder = Color(0xBA, 0xA7, 0x93)
private val colorBorderText = Color.White
private val borderTextStyle = TextStyle(color = colorBorderText)

private val DrawScope.borderSize
    get() = size.width / 24

@Composable
internal fun ChessBoardView(
    modifier: Modifier = Modifier,
    state: ChessBoardViewState,
    onSquareTap: (SquareNotation) -> Unit = {},
    onSquareLongPress: (SquareNotation) -> Unit = {}
) {
    val textMeasurer = rememberTextMeasurer()
    val piecePainters = remember { mutableMapOf<String, Painter>() }
    if (piecePainters.isEmpty()) {
        createPiecePainters(piecePainters)
    }

    Box(modifier = modifier
        .drawBehind {
            if (Dimensions.squareSideLength == 0f) {
                Dimensions.squareSideLength = (this.size.width - borderSize * 2) / 8f
                Dimensions.borderLength = borderSize
            }

            drawSquares()
            drawOverlaySquares(state.overlaySquares)
            drawBorder()
            drawRowNumbers(textMeasurer)
            drawColumnLetters(textMeasurer)
            drawPieces(state.pieces, piecePainters)
            drawArrows(state.arrows)
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onTap = { offset ->
                    offset
                        .toSquare()
                        ?.let { onSquareTap(it.notation) }
                },
                onLongPress = { offset ->
                    offset
                        .toSquare()
                        ?.let { onSquareLongPress(it.notation) }
                }
            )
        }
    )
}

private object Dimensions {
    var borderLength: Float = 0f

    var squareSideLength: Float = 0f
        set(value) {
            field = value
            squareSize = Size(value, value)
            pieceSize = Size(value * 0.9f, value * 0.9f)
            piecePadding = (value - pieceSize.width) / 2
        }

    var squareSize: Size = Size(0f, 0f)
        private set

    var pieceSize: Size = Size(0f, 0f)
        private set

    var piecePadding: Float = 0f
        private set
}

private fun DrawScope.drawPieces(pieces: Set<ChessBoardViewState.Piece>, painters: Map<String, Painter>) {
    pieces.forEach {
        val painter = checkNotNull(painters[it.drawableName]) { "No painter for ${it.drawableName}." }
        val topLeft = squares[it.position]!!.topLeftCorner
        val center = squares[it.position]!!.center

        with(painter) {
            scale(pivot = center, scale = if (it.isElevated) 1.2f else 1f) {
                translate(left = topLeft.x + Dimensions.piecePadding, top = topLeft.y + Dimensions.piecePadding) {
                    draw(size = Dimensions.pieceSize)
                }
            }
        }
    }
}

private fun Offset.toSquare() =
    squares.values.firstOrNull { square -> square.rect.contains(this.x, this.y) }

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

private fun DrawScope.drawOverlaySquares(overlaySquares: Set<ChessBoardViewState.OverlaySquare>) {
    overlaySquares.forEach {
        val overlaySquare = checkNotNull(squares[it.square])
        drawRect(
            color = Color(it.color).copy(alpha = 0.5f),
            topLeft = overlaySquare.topLeftCorner,
            size = Dimensions.squareSize
        )
    }
}

private fun DrawScope.drawArrows(arrows: Set<ChessBoardViewState.Arrow>) {
    arrows.forEach {
        drawArrow(
            color = Color(it.color),
            from = squares[it.start]!!.center,
            to = squares[it.end]!!.center,
            weight = it.weight,
            isStrong = it.strong
        )
    }
}

private fun DrawScope.drawArrow(color: Color, from: Offset, to: Offset, weight: Float, isStrong: Boolean) {
    val angleRad: Float

    //values to change for other appearance *CHANGE THESE FOR OTHER SIZE ARROWHEADS*
    val radius = 45f * weight
    val angle = 60f

    //some angle calculations
    angleRad = ((PI * angle / 180.0f).toFloat())
    val lineAngle: Float = atan2(to.y - from.y, to.x - from.x)

    //the line
    val x = to.x - radius * 0.7f * cos(lineAngle)
    val y = to.y - radius * 0.7f * sin(lineAngle)
    drawLine(
        color = color,
        start = from,
        end = Offset(x, y),
        strokeWidth = 16f * weight,
        pathEffect = if (!isStrong) PathEffect.dashPathEffect(floatArrayOf(16f * weight, 16f * weight)) else null
    )

    //the triangle
    val path = androidx.compose.ui.graphics.Path()
    path.fillType = PathFillType.EvenOdd
    path.moveTo(to.x, to.y)
    path.lineTo(
        (to.x - radius * cos(lineAngle - angleRad / 2.0)).toFloat(),
        (to.y - radius * sin(lineAngle - angleRad / 2.0)).toFloat()
    )
    path.lineTo(
        (to.x - radius * cos(lineAngle + angleRad / 2.0)).toFloat(),
        (to.y - radius * sin(lineAngle + angleRad / 2.0)).toFloat()
    )
    path.close()

    drawPath(
        path = path,
        color = color,
    )
}

//private val textSizesCache = mutableMapOf<Int, TextUnit>()
//
//@Suppress("SameParameterValue")
//private fun findTextSize(drawScopeSize: Size, symbol: String, textMeasurer: TextMeasurer): TextUnit {
//    return textSizesCache.getOrPut(drawScopeSize.height.toInt()) {
//        var size = 2
//
//        while (
//            textMeasurer.measure(symbol, TextStyle.Default + TextStyle(fontSize = size.sp)).size.height < Square.sideLength * 0.8
//        ) {
//            size++
//        }
//
//        size.sp
//    }
//}

private fun DrawScope.drawSquares() {
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
            topLeft = Offset(width - (borderSize + textWidth) / 2, squares["a$row"]!!.center.y - textHeight / 2),
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

@SuppressLint("ComposableNaming")
@Composable
private fun createPiecePainters(painters: MutableMap<String, Painter>) {
    painters["piece_bb"] = rememberVectorPainter(ImageVector.vectorResource(R.drawable.piece_bb))
    painters["piece_bw"] = rememberVectorPainter(ImageVector.vectorResource(R.drawable.piece_bw))
    painters["piece_kb"] = rememberVectorPainter(ImageVector.vectorResource(R.drawable.piece_kb))
    painters["piece_kw"] = rememberVectorPainter(ImageVector.vectorResource(R.drawable.piece_kw))
    painters["piece_nb"] = rememberVectorPainter(ImageVector.vectorResource(R.drawable.piece_nb))
    painters["piece_nw"] = rememberVectorPainter(ImageVector.vectorResource(R.drawable.piece_nw))
    painters["piece_pb"] = rememberVectorPainter(ImageVector.vectorResource(R.drawable.piece_pb))
    painters["piece_pw"] = rememberVectorPainter(ImageVector.vectorResource(R.drawable.piece_pw))
    painters["piece_qb"] = rememberVectorPainter(ImageVector.vectorResource(R.drawable.piece_qb))
    painters["piece_qw"] = rememberVectorPainter(ImageVector.vectorResource(R.drawable.piece_qw))
    painters["piece_rb"] = rememberVectorPainter(ImageVector.vectorResource(R.drawable.piece_rb))
    painters["piece_rw"] = rememberVectorPainter(ImageVector.vectorResource(R.drawable.piece_rw))
}

private data class Square(val row: Int, val column: Int) {
    val topLeftCorner: Offset
        get() = Offset(
            (column - 1) * Dimensions.squareSideLength + Dimensions.borderLength,
            (8 - row) * Dimensions.squareSideLength + Dimensions.borderLength
        )

    val center: Offset
        get() = Offset(topLeftCorner.x + Dimensions.squareSideLength / 2f, topLeftCorner.y + Dimensions.squareSideLength / 2f)

    val bottomRightCorner: Offset
        get() = Offset(
            topLeftCorner.x + Dimensions.squareSideLength,
            topLeftCorner.y + Dimensions.squareSideLength
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
            size = Dimensions.squareSize,
        )
    }
}

private const val COLUMN_LETTERS = "_abcdefgh"
