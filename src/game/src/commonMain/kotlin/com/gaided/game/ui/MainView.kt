package com.gaided.game.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gaided.game.GameViewModel
import com.gaided.game.ui.theme.GameTheme

@Composable
fun MainView(modifier: Modifier = Modifier) = GameTheme {
    Surface {
        val viewModel: GameViewModel = viewModel(
            GameViewModel::class,
            viewModelStoreOwner = LocalViewModelStoreOwner.current ?: appViewModelStoreOwner,
            factory = GameViewModel.Factory()
        )

        LaunchedEffect(viewModel) {
            viewModel.start()
        }

        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {

            PlayerView(
                state = viewModel.playerBlack.collectAsState(),
            )

            Spacer(modifier = Modifier.height(12.dp))

            ChessBoardView(
                state = viewModel.board,
                onSquareTap = viewModel::onSquareClick,
                onSquareLongPress = viewModel::onSquareLongClick,
            )

            Spacer(modifier = Modifier.height(12.dp))

            PlayerView(
                state = viewModel.playerWhite.collectAsState(),
            )
        }
    }
}

// In desktop app the LocalViewModelStoreOwner.current is null, so this one is used.
private val appViewModelStoreOwner = object : ViewModelStoreOwner {
    private val appViewModelStore = ViewModelStore()

    override val viewModelStore: ViewModelStore
        get() = appViewModelStore
}
