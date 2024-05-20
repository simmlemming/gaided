package com.gaided.game.ui

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gaided.game.GameViewModel
import com.gaided.game.ui.theme.GameTheme

@Composable
fun MainView() = GameTheme {
    Surface {
        val viewModel: GameViewModel = viewModel(
            GameViewModel::class,
            viewModelStoreOwner = LocalViewModelStoreOwner.current ?: appViewModelStoreOwner,
            factory = GameViewModel.Factory()
        )

        LaunchedEffect(viewModel) {
            viewModel.start()
        }

        chessBoardView(
            boardUiState = viewModel.board,
            onSquareTap = viewModel::onSquareClick,
            onSquareLongPress = viewModel::onSquareLongClick
        )
    }
}

// In desktop app the LocalViewModelStoreOwner.current is null, so this one is used.
private val appViewModelStoreOwner = object : ViewModelStoreOwner {
    private val appViewModelStore = ViewModelStore()

    override val viewModelStore: ViewModelStore
        get() = appViewModelStore
}
