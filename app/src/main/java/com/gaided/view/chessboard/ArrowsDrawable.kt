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
        paint: Paint,
        canvas: Canvas,
        from_x: Float,
        from_y: Float,
        to_x: Float,
        to_y: Float
    ) {
        val anglerad: Float

        //values to change for other appearance *CHANGE THESE FOR OTHER SIZE ARROWHEADS*
        val radius = 45f
        val angle = 60f

        //some angle calculations
        anglerad = ((PI * angle / 180.0f).toFloat())
        val lineangle: Float = atan2(to_y - from_y, to_x - from_x)

        //tha line
        canvas.drawLine(from_x, from_y, to_x, to_y, paint)

        //tha triangle
        val path = Path()
        path.fillType = Path.FillType.EVEN_ODD
        path.moveTo(to_x, to_y)
        path.lineTo(
            (to_x - radius * cos(lineangle - anglerad / 2.0)).toFloat(),
            (to_y - radius * sin(lineangle - anglerad / 2.0)).toFloat()
        )
        path.lineTo(
            (to_x - radius * cos(lineangle + anglerad / 2.0)).toFloat(),
            (to_y - radius * sin(lineangle + anglerad / 2.0)).toFloat()
        )
        path.close()
        canvas.drawPath(path, paint)
    }

    override fun setAlpha(alpha: Int) {}

    override fun setColorFilter(colorFilter: ColorFilter?) {}

    override fun getOpacity() = PixelFormat.OPAQUE
}