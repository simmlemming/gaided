package com.gaided.view.evaluation

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import com.gaided.R

internal class EvaluationView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val progressBarView: ProgressBar by lazy { findViewById(R.id.progress_bar) }
    private val progressTextView: TextView by lazy { findViewById(R.id.progress_text) }
    private val loadingView: View by lazy { findViewById(R.id.loading_progress) }

    override fun onFinishInflate() {
        super.onFinishInflate()
        inflate(context, R.layout.view_evaluation, this)
    }


    fun update(state: State) {
        loadingView.isVisible = state.isLoading

        state
            .takeIf { it != State.NULL && it != State.LOADING }
            ?.let {
                progressBarView.setProgress(it.value, true)
                progressTextView.text = "%.1f".format(it.value / 100f)
            }
    }

    internal data class State(
        val value: Int,
        val isLoading: Boolean,
    ) {
        companion object {
            val NULL = State(Int.MAX_VALUE, false)
            val LOADING = State(Int.MAX_VALUE, true)
            val INITIAL = State(0, false)
        }
    }
}