package com.gaided

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.gaided.engine.SquareNotation
import com.gaided.game.GameViewModel
import com.gaided.util.repeatOnResumed
import com.gaided.util.showMessageIfNotEmpty
import com.gaided.view.chessboard.ChessBoardView
import com.gaided.view.evaluation.EvaluationView
import com.gaided.view.player.PlayerView

internal class GameActivity : AppCompatActivity() {

    private val viewModel by viewModels<GameViewModel>(factoryProducer = { GameViewModel.Factory(
        config = GameViewModel.Factory.Config("", "", "")
    ) })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val playerWhiteView = findViewById<PlayerView>(R.id.player_white)
        val playerBlackView = findViewById<PlayerView>(R.id.player_black)

        val evaluationView = findViewById<EvaluationView>(R.id.evaluation)
        val boardView = findViewById<ChessBoardView>(R.id.board)

        repeatOnResumed {
            viewModel.board.collect {
                boardView.update(it, object : ChessBoardView.Listener {
                    override fun onSquareClick(square: SquareNotation) {
                        viewModel.onSquareClick(square)
                    }

                    override fun onSquareLongClick(square: SquareNotation) {
                        viewModel.onSquareLongClick(square)
                    }
                })
            }
        }

        repeatOnResumed {
            viewModel.playerWhite.collect {
                playerWhiteView.update(it)
            }
        }

        repeatOnResumed {
            viewModel.playerBlack.collect {
                playerBlackView.update(it)
            }
        }

        repeatOnResumed {
            viewModel.evaluation.collect {
                evaluationView.update(it)
            }
        }

        if (savedInstanceState == null) {
            viewModel.start()
        }

        repeatOnResumed {
            viewModel.userMessage.collect {
                showMessageIfNotEmpty(it) {
                    viewModel.onUserMessageShown()
                }
            }
        }
    }
}