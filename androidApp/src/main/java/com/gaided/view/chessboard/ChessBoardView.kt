package com.gaided.view.chessboard

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.OnGestureListener
import android.view.MotionEvent
import android.view.View
import com.gaided.engine.SquareNotation
import com.gaided.game.ui.model.ChessBoardViewState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@Suppress("LocalVariableName")
internal class ChessBoardView @JvmOverloads constructor(
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
    private val pieces = MutableStateFlow(PiecesDrawable(context, emptyMap(), emptySet()))
    private val arrows = MutableStateFlow(ArrowsDrawable(emptyMap(), emptySet()))
    private val overlaySquares = MutableStateFlow(OverlaySquaresDrawable(emptyMap(), emptySet()))
    private var listener: Listener? = null

    internal fun update(state: ChessBoardViewState, listener: Listener? = null) {
        this.listener = listener
        pieces.value = PiecesDrawable(context, squares, state.pieces)
        arrows.value = ArrowsDrawable(squares, state.arrows)
        overlaySquares.value = OverlaySquaresDrawable(squares, state.overlaySquares)
    }

    private val gestureDetector = GestureDetector(context, object : OnGestureListener {
        override fun onDown(e: MotionEvent) =
            e.toSquare() != null

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            val square = e.toSquare() ?: return false
            listener?.onSquareClick(square.notation)
            return true
        }

        override fun onScroll(p0: MotionEvent?, p1: MotionEvent, p2: Float, p3: Float) = false

        override fun onLongPress(e: MotionEvent) {
            e.toSquare()?.let {
                listener?.onSquareLongClick(it.notation)
            }
        }

        override fun onFling(p0: MotionEvent?, p1: MotionEvent, p2: Float, p3: Float) = false

        private fun MotionEvent.toSquare() = squares.values
            .firstOrNull { square -> square.rect.contains(this.x, this.y) }

        override fun onShowPress(e: MotionEvent) = Unit
    })

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        require(coroutineScope == null)

        coroutineScope = CoroutineScope(Dispatchers.Main).also {
            it.launch {
                pieces.collect { invalidate() }
            }
            it.launch {
                arrows.collect { invalidate() }
            }
            it.launch {
                overlaySquares.collect { invalidate() }
            }
        }
    }

    override fun onDetachedFromWindow() {
        checkNotNull(coroutineScope).cancel()
        coroutineScope = null
        super.onDetachedFromWindow()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        val width = canvas.width.toFloat()
        Square.sideLength = (width - borderSize * 2) / 8f
        Square.borderLength = borderSize

        for (square in squares.values) {
            square.draw(canvas, square.toPaint())
        }

        drawBorder(canvas)
        drawRowNumbers(canvas)
        drawColumnLetters(canvas)

        with(overlaySquares.value) {
            setBounds(0, 0, canvas.width, canvas.height)
            draw(canvas)
        }

        with(pieces.value) {
            setBounds(0, 0, canvas.width, canvas.height)
            draw(canvas)
        }

        with(arrows.value) {
            setBounds(0, 0, canvas.width, canvas.height)
            draw(canvas)
        }
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

    internal data class Square(val row: Int, val column: Int) {
        val topLeftCorner: PointF
            get() = PointF(
                (column - 1) * sideLength + borderLength,
                (8 - row) * sideLength + borderLength
            )

        val center: PointF
            get() = PointF(topLeftCorner.x + sideLength / 2f, topLeftCorner.y + sideLength / 2f)

        val bottomRightCorner: PointF
            get() = PointF(
                topLeftCorner.x + sideLength,
                topLeftCorner.y + sideLength
            )

        val rect: RectF
            get() = RectF(
                topLeftCorner.x, topLeftCorner.y,
                bottomRightCorner.x, bottomRightCorner.y
            )

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

    internal interface Listener {
        fun onSquareClick(square: SquareNotation)
        fun onSquareLongClick(square: SquareNotation)
    }
}

private const val COLUMN_LETTERS = "_abcdefgh"
