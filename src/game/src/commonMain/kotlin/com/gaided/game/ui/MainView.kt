package com.gaided.game.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.gaided.game.GameViewModel

@Composable
fun MainView(viewModel: GameViewModel) {
    LaunchedEffect(viewModel) {
        viewModel.start()
    }

    chessBoardView(
        boardUiState = viewModel.board,
        onSquareTap = viewModel::onSquareClick,
        onSquareLongPress = viewModel::onSquareLongClick
    )
}
