package com.gaided.fortress.app.android.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun FortressPlayerView(
    state: FortressPlayerViewState,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .height(48.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = state.icon,
                contentDescription = "Computer Icon",
                tint = Color.Gray
            )

            Spacer(modifier = Modifier.size(8.dp))

            Text(
                text = state.name,
                color = Color.Gray,
            )

            if (state.progressVisible) {
                Spacer(modifier = Modifier.size(4.dp))
                Icon(
                    imageVector = Icons.Default.HourglassBottom,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

data class FortressPlayerViewState(
    val name: String,
    val icon: ImageVector,
    val progressVisible: Boolean,
) {
    companion object {
        val EMPTY = FortressPlayerViewState(
            name = "-",
            icon = Icons.Default.HourglassBottom,
            progressVisible = false
        )
    }
}

@Preview(widthDp = 360)
@Composable
fun FortressPlayerViewPreview() {
    FortressPlayerView(
        state = FortressPlayerViewState(
            name = "Stockfish 15",
            icon = Icons.Default.Computer,
            progressVisible = true
        )
    )
}
