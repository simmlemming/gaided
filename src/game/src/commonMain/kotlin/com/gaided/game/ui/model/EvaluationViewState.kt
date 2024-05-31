package com.gaided.game.ui.model

import androidx.compose.runtime.Immutable

@Immutable
data class EvaluationViewState(
    val value: Int,
    val isLoading: Boolean,
) {
    companion object {
        val NULL = EvaluationViewState(Int.MAX_VALUE, false)
        val LOADING = EvaluationViewState(Int.MAX_VALUE, true)
        val INITIAL = EvaluationViewState(0, false)
    }
}