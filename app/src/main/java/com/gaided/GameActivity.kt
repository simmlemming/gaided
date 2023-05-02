package com.gaided

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.gaided.view.chessboard.ChessBoardView
import com.gaided.view.player.PlayerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class GameActivity : AppCompatActivity() {

    private val viewModel by viewModels<GameViewModel>(factoryProducer = { GameViewModel.Factory() })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val player1View = findViewById<PlayerView>(R.id.player1)
        val player2View = findViewById<PlayerView>(R.id.player2)

        val boardView = findViewById<ChessBoardView>(R.id.board)

        repeatOnResumed {
            viewModel.boardState.collect {
                boardView.update(it)
            }
        }

        repeatOnResumed {
            viewModel.player1.collect {
                player1View.update(it, object : PlayerView.Listener {
                    override fun onMoveClick(id: Int) {
                        viewModel.onPlayer1MoveSelected(id)
                    }
                })
            }
        }

        repeatOnResumed {
            viewModel.player2.collect {
                player2View.update(it, object : PlayerView.Listener {
                    override fun onMoveClick(id: Int) {
                        viewModel.onPlayer2MoveSelected(id)
                    }
                })
            }
        }

        if (savedInstanceState == null) {
            viewModel.start()
        }

        lifecycleScope.launch {
            delay(3000)
            viewModel.move("e2", "e3")
        }

        repeatOnResumed {
            viewModel.userMessage.collect {
                showMessageIfNotEmpty(it) {
                    viewModel.userMessageShown()
                }
            }
        }
    }
}