package com.gaided.view.player

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.gaided.R

internal class PlayerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val progressView: View by lazy { findViewById(R.id.progress_view) }
    private val move1StatsView: TextView by lazy { findViewById(R.id.move_1_stats) }
    private val move2StatsView: TextView by lazy { findViewById(R.id.move_2_stats) }
    private val move3StatsView: TextView by lazy { findViewById(R.id.move_3_stats) }

    override fun onFinishInflate() {
        super.onFinishInflate()
        inflate(context, R.layout.view_player, this)
    }

    fun update(state: State) {
        progressView.visibility = if (state.progressVisible) View.VISIBLE else View.GONE
        move1StatsView.setTextOrGone(state.movesStats.getOrNull(0)?.text)
        move2StatsView.setTextOrGone(state.movesStats.getOrNull(1)?.text)
        move3StatsView.setTextOrGone(state.movesStats.getOrNull(2)?.text)
    }

    private fun TextView.setTextOrGone(text: String?) {
        isVisible = (text != null)
        this.text = text
    }

    data class State(
        val progressVisible: Boolean,
        val movesStats: List<Stats>
    ) {
        companion object {
            val EMPTY = State(false, emptyList())
            val OPPONENT_MOVE = EMPTY
        }
    }

    data class Stats(
        val number: Int,
        val total: Int,
        val text: String
    )
}