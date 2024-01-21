package com.gaided.view.evaluation

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ProgressBar
import com.gaided.R

internal class EvaluationView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val progressView: ProgressBar by lazy { findViewById(R.id.progress) }

    override fun onFinishInflate() {
        super.onFinishInflate()
        inflate(context, R.layout.view_evaluation, this)
    }

    fun update(state: State) {
        state
            .takeIf { it != State.NULL }
            ?.let { progressView.setProgress(it.value, true) }
    }

    internal data class State(val value: Int) {
        companion object {
            val NULL = State(Int.MAX_VALUE)
        }
    }
}