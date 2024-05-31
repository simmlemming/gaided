package com.gaided.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun VerticalProgressView(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress = animateFloatAsState(targetValue = progress)

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .width(16.dp)
    ) {
        val weight: Float = animatedProgress.value.coerceIn(0.001f, 0.999f)

        Box(
            modifier = Modifier
                .background(Color.Black)
                .weight(1f - weight)
                .fillMaxWidth()
        )

        Box(
            modifier = Modifier
                .weight(weight)
                .fillMaxWidth()
                .background(Color.White)
        )
    }
}
