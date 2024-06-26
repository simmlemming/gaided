package com.gaided.game.ui.model

import androidx.compose.runtime.Immutable

@Immutable
data class PlayerViewState(
    val progressVisible: Boolean,
    val movesStats: List<PlayerViewStats>
) {
    companion object {
        val EMPTY = PlayerViewState(false, emptyList())
        val OPPONENT_MOVE = EMPTY
    }
}