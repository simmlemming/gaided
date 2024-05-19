package com.gaided.game.ui

import androidx.compose.runtime.Composable
import com.gaided.game.GameViewModel

@Composable
fun MainView(viewModel: GameViewModel) {
    chessBoardView(
        boardUiState = viewModel.board,
        onSquareTap = viewModel::onSquareClick,
        onSquareLongPress = viewModel::onSquareLongClick
    )
}
