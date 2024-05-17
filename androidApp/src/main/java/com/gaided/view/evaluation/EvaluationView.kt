package com.gaided.view.evaluation

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import com.gaided.R
import com.gaided.game.ui.model.EvaluationViewState

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


    fun update(state: EvaluationViewState) {
        loadingView.isVisible = state.isLoading

        state
            .takeIf { it != EvaluationViewState.NULL && it != EvaluationViewState.LOADING }
            ?.let {
                progressBarView.setProgress(it.value, true)
                progressTextView.text = "%.1f".format(it.value / 100f)
            }
    }

}