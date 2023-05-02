package com.gaided.view.player

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import com.gaided.R
import com.gaided.domain.MoveNotation
import com.gaided.domain.SquareNotation

internal class PlayerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val progressView: View by lazy { findViewById(R.id.progress_view) }
    private val move1View: Button by lazy { findViewById(R.id.move_1) }
    private val move2View: Button by lazy { findViewById(R.id.move_2) }
    private val move3View: Button by lazy { findViewById(R.id.move_3) }

    override fun onFinishInflate() {
        super.onFinishInflate()
        inflate(context, R.layout.view_player, this)
    }

    fun update(state: State, listener: Listener) {
        progressView.visibility = if (state.progressVisible) View.VISIBLE else View.GONE
        move1View.update(state.move1)
        move2View.update(state.move2)
        move3View.update(state.move3)

        move1View.setOnClickListener {
            listener.onMoveClick(state.move1.move)
        }

        move2View.setOnClickListener {
            listener.onMoveClick(state.move2.move)
        }

        move3View.setOnClickListener {
            listener.onMoveClick(state.move3.move)
        }
    }

    private fun Button.update(state: State.Move) {
        visibility = if (state.isVisible) View.VISIBLE else View.INVISIBLE
        text = state.text
    }

    data class State(
        val progressVisible: Boolean,
        val move1: Move,
        val move2: Move,
        val move3: Move
    ) {

        data class Move(
            val move: MoveNotation,
            val isVisible: Boolean,
            val text: String
        )

        companion object {
            val EMPTY = State(
                false,
                Move("", false, ""),
                Move("", false, ""),
                Move("", false, ""),
            )

            val OPPONENT_MOVE = EMPTY
        }
    }

    interface Listener {
        fun onMoveClick(move: MoveNotation)
    }
}