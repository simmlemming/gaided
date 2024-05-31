package com.gaided.game.ui.model

import androidx.compose.runtime.Immutable

@Immutable
data class PlayerViewStats(
    val number: Int,
    val total: Int,
    val text: String
)