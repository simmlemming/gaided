package com.gaided.fortress.app.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gaided.chessui.model.ChessBoardViewState
import com.gaided.logger.Logger


class FortressActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.enabled = true
        setContent {
            FortressApp()
        }
    }
}

@Composable
private fun FortressApp() {
    val viewModel: FortressViewModel = viewModel(
        FortressViewModel::class,
        factory = FortressViewModel.Factory()
    )

    val chessBoard by viewModel.chessBoardViewState.collectAsStateWithLifecycle(ChessBoardViewState.EMPTY)

    LaunchedEffect(viewModel) {
        viewModel.startFortressGame()
        viewModel.userMessage.collect {
            Logger.i(it)
        }
    }

    FortressScreen(
        chessBoard = chessBoard,
        onSquareClick = viewModel::onSquareClick,
        onSquareLongClick = viewModel::onSquareLongClick,
    )
}
