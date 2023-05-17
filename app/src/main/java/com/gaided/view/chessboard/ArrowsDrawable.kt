package com.gaided.view.chessboard

import android.graphics.*
import android.graphics.drawable.Drawable
import com.gaided.domain.SquareNotation
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

internal class ArrowsDrawable(
    private val squares: Map<SquareNotation, ChessBoardView.Square>,
    private val arrows: Set<ChessBoardView.State.Arrow>,
) : Drawable() {
    override fun draw(canvas: Canvas) {
        for (arrow in arrows) {
            val paint = Paint().apply {
                color = arrow.color
                strokeWidth = 16f
            }
            val start = squares[arrow.start]!!.center
            val end = squares[arrow.end]!!.center

            drawArrow(paint, canvas, start.x, start.y, end.x, end.y)
        }
    }

    private fun drawArrow(
        paint: Paint, canvas: Canvas,
        fromX: Float, fromY: Float, toX: Float, toY: Float
    ) {
        val angleRad: Float

        //values to change for other appearance *CHANGE THESE FOR OTHER SIZE ARROWHEADS*
        val radius = 45f
        val angle = 60f

        //some angle calculations
        angleRad = ((PI * angle / 180.0f).toFloat())
        val lineAngle: Float = atan2(toY - fromY, toX - fromX)

        //tha line
        canvas.drawLine(fromX, fromY, toX, toY, paint)

        //tha triangle
        val path = Path()
        path.fillType = Path.FillType.EVEN_ODD
        path.moveTo(toX, toY)
        path.lineTo(
            (toX - radius * cos(lineAngle - angleRad / 2.0)).toFloat(),
            (toY - radius * sin(lineAngle - angleRad / 2.0)).toFloat()
        )
        path.lineTo(
            (toX - radius * cos(lineAngle + angleRad / 2.0)).toFloat(),
            (toY - radius * sin(lineAngle + angleRad / 2.0)).toFloat()
        )
        path.close()
        canvas.drawPath(path, paint)
    }

    override fun setAlpha(alpha: Int) {}

    override fun setColorFilter(colorFilter: ColorFilter?) {}

    override fun getOpacity() = PixelFormat.OPAQUE
}