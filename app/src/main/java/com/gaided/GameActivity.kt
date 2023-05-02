package com.gaided

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.gaided.view.chessboard.ChessBoardView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class GameActivity : AppCompatActivity() {

    private val viewModel by viewModels<GameViewModel>(factoryProducer = { GameViewModel.Factory() })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val boardView = findViewById<ChessBoardView>(R.id.board)

        repeatOnResumed {
            viewModel.boardState.collect {
                boardView.update(it)
            }
        }

        if (savedInstanceState == null) {
            viewModel.start()
        }

        lifecycleScope.launch {
            delay(3000)
            viewModel.move("e2", "e4")
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