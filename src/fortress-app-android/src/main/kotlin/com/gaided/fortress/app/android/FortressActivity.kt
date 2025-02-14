package com.gaided.fortress.app.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gaided.chessui.model.ChessBoardViewState


class FortressActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FortressApp()
        }
    }
}

@Composable
private fun FortressApp() {
    val viewModel: FortressGameViewModel = viewModel(
        FortressGameViewModel::class,
        factory = FortressGameViewModel.Factory()
    )

    val chessBoard by viewModel.chessBoardViewState.collectAsStateWithLifecycle(ChessBoardViewState.EMPTY)
    GameScreen(
        chessBoard = chessBoard,
        onSquareClick = viewModel::onSquareClick,
        onSquareLongClick = viewModel::onSquareLongClick,
    )
}
