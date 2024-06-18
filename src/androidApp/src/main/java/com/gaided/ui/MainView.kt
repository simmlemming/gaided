package com.gaided.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gaided.game.GameViewModel
import com.gaided.ui.theme.GameTheme

@Composable
fun MainView(modifier: Modifier = Modifier) = GameTheme {
    Surface {
        val viewModel: GameViewModel = viewModel(
            GameViewModel::class,
            factory = GameViewModel.Factory(
                config = GameViewModel.Factory.Config(
                    remoteBoardUrl = "http://10.0.2.2:8080",
                    stockfishEngineUrl = "http://10.0.2.2:8081",
                    openAiApiKey = "..."
                )
            )
        )

        val boardViewState by viewModel.board.collectAsState()
        val evaluationViewState by viewModel.evaluation.collectAsState()
        val playerWhiteViewState by viewModel.playerWhite.collectAsState()
        val playerBlackViewState by viewModel.playerBlack.collectAsState()

        LaunchedEffect(viewModel) {
            viewModel.start()
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier.fillMaxSize()
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = modifier
                    .fillMaxWidth()
                    .height(intrinsicSize = IntrinsicSize.Max)
            ) {
                PlayerView(
                    state = playerBlackViewState,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.height(IntrinsicSize.Max)
                ) {
                    EvaluationView(
                        state = evaluationViewState,
                        modifier = Modifier
                            .width(16.dp)
                            .fillMaxHeight()
                    )

                    ChessBoardView(
                        state = boardViewState,
                        onSquareTap = viewModel::onSquareClick,
                        onSquareLongPress = viewModel::onSquareLongClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                PlayerView(
                    state = playerWhiteViewState,
                )
            }
        }
    }
}
