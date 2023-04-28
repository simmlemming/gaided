package com.gaided.view

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

internal typealias SquareNotation = String

@Suppress("LocalVariableName")
internal class ChessBoard @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paintDarkSquare = Paint().apply {
        color = Color.parseColor("#BA9772")
    }

    private val paintLightSquare = Paint().apply {
        color = Color.parseColor("#F1DFC0")
    }

    private val paintBorder = Paint().apply {
        color = Color.parseColor("#BAA793")
    }

    private val paintBorderText = Paint().apply {
        color = Color.parseColor("#FFFFFF")
        textAlign = Paint.Align.CENTER
        textSize = 36f
    }

    private val paintBlackPieces = Paint().apply {
        color = Color.BLACK
    }

    private val squares: Map<SquareNotation, Square>

    private val borderSize = 48f

    init {
        val _squares = mutableMapOf<SquareNotation, Square>()

        for (row in 1..8) {
            for (column in 1..8) {
                val square = Square(row, column)
                _squares[square.notation] = square
            }
        }

        squares = _squares.toMap()
    }

    private var coroutineScope: CoroutineScope? = null
    private val pieces = MutableStateFlow(PiecesDrawable(setOf(), paintBlackPieces))

    internal fun update(state: State) {
        pieces.value = PiecesDrawable(state.pieces, paintBlackPieces)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        require(coroutineScope == null)

        coroutineScope = CoroutineScope(Dispatchers.Main).also {
            it.launch {
                pieces.collect { invalidate() }
            }
        }
    }

    override fun onDetachedFromWindow() {
        checkNotNull(coroutineScope).cancel()
        coroutineScope = null
        super.onDetachedFromWindow()
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        canvas ?: return

        val width = canvas.width.toFloat()
        Square.sideLength = (width - borderSize * 2) / 8f
        Square.borderLength = borderSize

        for (square in squares.values) {
            square.draw(canvas, square.toPaint())
        }

        drawBorder(canvas)
        drawRowNumbers(canvas)
        drawColumnLetters(canvas)

        with(pieces.value) {
            setBounds(0, 0, canvas.width, canvas.height)
            draw(canvas)
        }
    }

    private inner class PiecesDrawable(
        private val pieces: Set<State.Piece>,
        private val blackPaint: Paint
    ) : Drawable() {
        override fun draw(canvas: Canvas) {
            for (piece in pieces) {
                val center = squares[piece.position]!!.center
                canvas.drawCircle(center.x, center.y, 24f, blackPaint)
            }
        }

        override fun setAlpha(alpha: Int) {}

        override fun setColorFilter(colorFilter: ColorFilter?) {}

        override fun getOpacity(): Int = PixelFormat.OPAQUE

        override fun equals(other: Any?) =
            this.pieces == (other as? PiecesDrawable)?.pieces

        override fun hashCode() = pieces.hashCode()
    }

    private fun drawBorder(canvas: Canvas) {
        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()

        canvas.drawRect(0f, 0f, width, borderSize, paintBorder)
        canvas.drawRect(0f, height - borderSize, width, height, paintBorder)
        canvas.drawRect(0f, 0f, borderSize, height, paintBorder)
        canvas.drawRect(width - borderSize, 0f, width, height, paintBorder)
    }

    private fun drawRowNumbers(canvas: Canvas) {
        val width = canvas.width.toFloat()
        val bounds = Rect()

        for (row in 1..8) {
            val text = row.toString()
            paintBorderText.getTextBounds(text, 0, text.length, bounds)
            val centerX = width - borderSize / 2
            val centerY = squares["a$row"]!!.center.y

            canvas.drawText(
                text,
                centerX,
                centerY + bounds.height() / 2,
                paintBorderText
            )
        }
    }

    private fun drawColumnLetters(canvas: Canvas) {
        val height = canvas.height.toFloat()
        val bounds = Rect()

        for (column in 1..8) {
            val letter = COLUMN_LETTERS[column].toString()
            paintBorderText.getTextBounds(letter, 0, letter.length, bounds)
            val centerX = squares["${letter}1"]!!.center.x
            val centerY = height - borderSize / 2 + bounds.height() / 2

            canvas.drawText(
                letter,
                centerX,
                centerY,
                paintBorderText
            )
        }
    }

    private fun Square.toPaint() = if ((row + column) % 2 == 0) {
        paintDarkSquare
    } else {
        paintLightSquare
    }

    private fun launch(block: suspend CoroutineScope.() -> Unit) =
        coroutineScope?.launch(block = block)

    internal data class State(val pieces: Set<Piece>) {
        companion object {
            val EMPTY = State(setOf())
        }

        internal data class Piece(val position: SquareNotation)
    }
}

private data class Square(val row: Int, val column: Int) {
    val topLeftCorner: PointF
        get() = PointF(
            (column - 1) * sideLength + borderLength,
            (8 - row) * sideLength + borderLength
        )

    val center: PointF
        get() = PointF(topLeftCorner.x + sideLength / 2f, topLeftCorner.y + sideLength / 2f)

    val notation: String = "${COLUMN_LETTERS[column]}$row"

    fun draw(canvas: Canvas, paint: Paint) {
        canvas.drawRect(
            topLeftCorner.x,
            topLeftCorner.y,
            topLeftCorner.x + sideLength,
            topLeftCorner.y + sideLength,
            paint
        )
    }

    companion object {
        var sideLength: Float = 0f
        var borderLength: Float = 0f
    }
}

private const val COLUMN_LETTERS = "_abcdefgh"
