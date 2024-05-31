package com.gaided.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gaided.game.ui.model.EvaluationViewState

private const val EVALUATION_MAX = 800

@Composable
fun EvaluationView(state: EvaluationViewState, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(Color.Red),
        contentAlignment = Alignment.BottomCenter
    ) {
        // state.value is -Inf..+Inf, progress is 0..1.
        // 0 value is 0.5 progress.
        val coercedValue = state.value.coerceIn(-EVALUATION_MAX, EVALUATION_MAX).toFloat()
        val progress = (coercedValue + EVALUATION_MAX) / (EVALUATION_MAX * 2)

        VerticalProgressView(
            progress = progress,
            modifier = Modifier.fillMaxSize(),
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    strokeWidth = 1.dp,
                    modifier = Modifier.size(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "%.1f".format(state.value / 100f),
                maxLines = 1,
                color = Color.Black,
                fontSize = 8.sp,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
    }
}

@Preview(widthDp = 16)
@Composable
fun EvaluationViewPreview() {
    EvaluationView(
        state = EvaluationViewState(
            value = 200,
            isLoading = true
        )
    )
}