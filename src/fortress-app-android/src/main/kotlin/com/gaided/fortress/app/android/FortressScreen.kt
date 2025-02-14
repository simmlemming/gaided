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
import com.gaided.model.SquareNotation

@Composable
internal fun FortressScreen(
    chessBoard: ChessBoardViewState,
    onSquareClick: (SquareNotation) -> Unit = {},
    onSquareLongClick: (SquareNotation) -> Unit = {},
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        ChessBoardView(
            state = chessBoard,
            onSquareTap = onSquareClick,
            onSquareLongPress = onSquareLongClick,
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(1f)
        )
    }
}

@Preview
@Composable
fun FortressScreenPreview() {
    FortressScreen(ChessBoardViewState.EMPTY)
}
