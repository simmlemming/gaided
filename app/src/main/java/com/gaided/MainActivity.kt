package com.gaided

import android.graphics.Color
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
                delay(2000)
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
    val pieces = randomPieces()
    return ChessBoard.State(pieces, randomArrows(pieces))
}

private fun randomArrows(pieces: Set<ChessBoard.State.Piece>): Set<ChessBoard.State.Arrow> {
    val numberOfArrows = 3
    return (0..numberOfArrows)
        .map {
            val start = pieces.random(rnd).position
            val end = randomNotation()
            ChessBoard.State.Arrow(start, end, Color.parseColor("#648EBA"))
        }
        .toSet()
}

private fun randomPieces(): Set<ChessBoard.State.Piece> {
    val numberOfPieces = rnd.nextInt(16) + 16
    return (0..numberOfPieces)
        .map { randomNotation() }
        .map { ChessBoard.State.Piece(it) }
        .toSet()
}

private fun randomNotation(): SquareNotation {
    val row = rnd.nextInt(8) + 1
    val column = rnd.nextInt(8) + 1
    return "${"_abcdefgh"[column]}${row}"
}
