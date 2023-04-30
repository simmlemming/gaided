package com.gaided

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.gaided.api.StockfishApi
import com.gaided.view.chessboard.ChessBoard
import com.gaided.view.chessboard.SquareNotation
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
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

        val api = StockfishApi("http://10.0.2.2:8080")
        val engine = Engine(api)

        lifecycleScope.launch {
            val response = engine.getFenPosition()
            Log.i("Gaided", response)
        }
    }
}

private val rnd = Random(System.currentTimeMillis())
private fun randomState(): ChessBoard.State {
    val whitePieces = randomWhitePieces()
    return ChessBoard.State(whitePieces + randomBlackPieces(), randomArrows(whitePieces))
}

private fun randomArrows(pieces: Set<ChessBoard.State.Piece>): Set<ChessBoard.State.Arrow> {
    val numberOfArrows = 3
    return (0 until numberOfArrows)
        .map {
            val start = pieces.random(rnd).position
            val end = randomNotation(1, 2, 3, 4, 5, 6, 7, 8)
            ChessBoard.State.Arrow(start, end, Color.parseColor("#648EBA"))
        }
        .toSet()
}

private fun randomWhitePieces(): Set<ChessBoard.State.Piece> {
    val numberOfPieces = rnd.nextInt(8) + 4
    return (0..numberOfPieces)
        .map { randomNotation(1, 2, 3, 4, 5) }
        .map { ChessBoard.State.Piece(it, Color.WHITE) }
        .toSet()
}

private fun randomBlackPieces(): Set<ChessBoard.State.Piece> {
    val numberOfPieces = rnd.nextInt(8) + 4
    return (0..numberOfPieces)
        .map { randomNotation(4, 5, 6, 7, 8) }
        .map { ChessBoard.State.Piece(it, Color.BLACK) }
        .toSet()
}

private fun randomNotation(vararg rows: Int): SquareNotation {
    val row = rows.random(rnd)
    val column = rnd.nextInt(8) + 1
    return "${"_abcdefgh"[column]}${row}"
}
