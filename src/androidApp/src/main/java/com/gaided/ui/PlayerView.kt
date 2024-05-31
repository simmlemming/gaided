package com.gaided.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gaided.game.ui.model.PlayerViewState

@Composable
fun PlayerView(state: PlayerViewState, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
    ) {
        if (state.progressVisible) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Preview
@Composable
fun PlayerViewPreview() {
    PlayerView(
        state = PlayerViewState(
            progressVisible = true,
            movesStats = emptyList()
        )
    )
}