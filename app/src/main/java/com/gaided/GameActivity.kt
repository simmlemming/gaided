package com.gaided

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.gaided.domain.MoveNotation
import com.gaided.view.chessboard.ChessBoardView
import com.gaided.view.player.PlayerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class GameActivity : AppCompatActivity() {

    private val viewModel by viewModels<GameViewModel>(factoryProducer = { GameViewModel.Factory() })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val playerWhiteView = findViewById<PlayerView>(R.id.player_white)
        val playerBlackView = findViewById<PlayerView>(R.id.player_black)

        val boardView = findViewById<ChessBoardView>(R.id.board)

        repeatOnResumed {
            viewModel.board.collect {
                boardView.update(it)
            }
        }

        repeatOnResumed {
            viewModel.playerWhite.collect {
                playerWhiteView.update(it, object : PlayerView.Listener {
                    override fun onMoveClick(move: MoveNotation) {
                        viewModel.onMoveClick(Game.Player.White, move)
                    }
                })
            }
        }

        repeatOnResumed {
            viewModel.playerBlack.collect {
                playerBlackView.update(it, object : PlayerView.Listener {
                    override fun onMoveClick(move: MoveNotation) {
                        viewModel.onMoveClick(Game.Player.Black, move)
                    }
                })
            }
        }

        if (savedInstanceState == null) {
            viewModel.start()
        }

//        lifecycleScope.launch {
//            delay(3000)
//            viewModel.move("e2e3")
//        }

        repeatOnResumed {
            viewModel.userMessage.collect {
                showMessageIfNotEmpty(it) {
                    viewModel.onUserMessageShown()
                }
            }
        }
    }
}