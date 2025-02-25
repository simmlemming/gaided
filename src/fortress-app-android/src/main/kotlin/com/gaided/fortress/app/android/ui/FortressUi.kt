package com.gaided.fortress.app.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun FortressPlayerView(
    state: FortressPlayerViewState,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
    ) {
        if (state.progressVisible) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(Color.Gray, shape = CircleShape)
            )
        }
    }
}

data class FortressPlayerViewState(
    val progressVisible: Boolean
)

@Preview(widthDp = 360)
@Composable
fun FortressPlayerViewPreview() {
    FortressPlayerView(
        state = FortressPlayerViewState(
            progressVisible = true
        )
    )
}
