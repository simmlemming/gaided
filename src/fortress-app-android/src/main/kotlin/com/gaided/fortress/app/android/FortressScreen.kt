package com.gaided.fortress.app.android

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gaided.chessui.ChessBoardView
import com.gaided.chessui.model.ChessBoardViewState
import com.gaided.fortress.app.android.ui.FortressPlayerView
import com.gaided.fortress.app.android.ui.FortressPlayerViewState
import com.gaided.model.SquareNotation

@Composable
internal fun FortressScreen(
    chessBoard: ChessBoardViewState,
    playerWhiteState: FortressPlayerViewState,
    playerBlackState: FortressPlayerViewState,
    onSquareClick: (SquareNotation) -> Unit = {},
    onSquareLongClick: (SquareNotation) -> Unit = {},
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF333333))
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
        ) {
            FortressPlayerView(
                state = playerBlackState,
            )

            ChessBoardView(
                state = chessBoard,
                onSquareTap = onSquareClick,
                onSquareLongPress = onSquareLongClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )

            FortressPlayerView(
                state = playerWhiteState,
            )
        }
    }
}

@Preview
@Composable
fun FortressScreenPreview() {
    FortressScreen(
        chessBoard = ChessBoardViewState.EMPTY,
        playerWhiteState = FortressPlayerViewState(progressVisible = true),
        playerBlackState = FortressPlayerViewState(progressVisible = false),
    )
}
