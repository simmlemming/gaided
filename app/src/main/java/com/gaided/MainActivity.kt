package com.gaided

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.gaided.view.chessboard.ChessBoard
import com.gaided.view.chessboard.SquareNotation
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val boardView = findViewById<ChessBoard>(R.id.board)

        val state = flow {
            while (currentCoroutineContext().isActive) {
                emit(randomState())
                delay(1000)
            }
        }

        lifecycleScope.launch {
            state.collect {
                boardView.update(it)
            }
        }
    }
}

private val rnd = Random(System.currentTimeMillis())
private fun randomState(): ChessBoard.State {
    val numberOfPieces = rnd.nextInt(16) + 16
    val pieces = (0..numberOfPieces)
        .map {
            val row = rnd.nextInt(8) + 1
            val column = rnd.nextInt(8) + 1
            row to "_abcdefgh"[column]
        }
        .map { ChessBoard.State.Piece("${it.second}${it.first}") }
        .toSet()

    return ChessBoard.State(pieces)
}

private fun Set<SquareNotation>.toState() = ChessBoard.State(
    map { ChessBoard.State.Piece(it) }.toSet()
)
