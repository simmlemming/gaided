package com.gaided.fortress.app.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gaided.logger.Logger

/**
 * TODO in the Fortress app
 *
 * - Move player view state to teh ChessViewModel.
 * - Detect when game ends.
 * - Support config changes.
 * - Support starting from a custom position.
 */

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

    val chessBoard by viewModel.board.collectAsStateWithLifecycle()
    val playerWhite by viewModel.playerWhite.collectAsStateWithLifecycle()
    val playerBlack by viewModel.playerBlack.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.startFortressGame(FortressViewModel.PlayerType.Stockfish, FortressViewModel.PlayerType.Stockfish)
        viewModel.userMessage.collect {
            Logger.i(it)
        }
    }

    FortressScreen(
        chessBoard = chessBoard,
        playerWhiteState = playerWhite,
        playerBlackState = playerBlack,
        onSquareClick = viewModel::onSquareClick,
        onSquareLongClick = viewModel::onSquareLongClick,
    )
}
