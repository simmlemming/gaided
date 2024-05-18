package com.gaided.game.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.gaided.game.GameViewModel
import com.gaided.game.ui.model.ChessBoardViewState
import kotlinx.coroutines.flow.Flow

@Composable
fun MainView(viewModel: GameViewModel) {
    boardView(viewModel.board)
}

@Composable
fun boardView(boardState: Flow<ChessBoardViewState>, modifier: Modifier = Modifier) {
    val state = boardState.collectAsState(ChessBoardViewState.EMPTY)
    Text(
        text = state.value.toString()
    )
}