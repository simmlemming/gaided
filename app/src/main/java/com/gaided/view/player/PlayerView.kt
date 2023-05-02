package com.gaided.view.player

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import com.gaided.R

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
        progressView.visibility = if (state.progressVisible) View.VISIBLE else View.INVISIBLE
        move1View.update(state.move1)
        move2View.update(state.move2)
        move3View.update(state.move3)

        move1View.setOnClickListener {
            listener.onMoveClick(state.move1.id)
        }

        move2View.setOnClickListener {
            listener.onMoveClick(state.move2.id)
        }

        move3View.setOnClickListener {
            listener.onMoveClick(state.move3.id)
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
        val move3: Move,
    ) {
        data class Move(
            val id: Int,
            val isVisible: Boolean,
            val text: String
        )

        companion object {
            val EMPTY = State(
                false,
                Move(0, false, ""),
                Move(1, false, ""),
                Move(2, false, ""),
            )
        }
    }

    interface Listener {
        fun onMoveClick(id: Int)
    }
}