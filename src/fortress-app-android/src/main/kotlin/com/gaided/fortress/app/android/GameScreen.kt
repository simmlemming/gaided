package com.gaided.fortress.app.android

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.gaided.chessui.ChessBoardView
import com.gaided.chessui.model.ChessBoardViewState

@Composable
internal fun GameScreen(chessBoard: ChessBoardViewState) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        ChessBoardView(
            state = chessBoard,
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(1f)
        )
    }
}

@Preview
@Composable
fun AppPreview() {
    GameScreen(ChessBoardViewState.EMPTY)
}
