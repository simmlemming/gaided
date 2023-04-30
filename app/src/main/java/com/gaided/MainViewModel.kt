package com.gaided

import android.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gaided.api.StockfishApi
import com.gaided.domain.Board
import com.gaided.view.chessboard.ChessBoardView
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

internal class MainViewModel(
    private val engine: Engine,
    private val board: Board
) : ViewModel() {
    val boardState = combine(board.pieces, board.arrows) { pieces, arrows ->
        ChessBoardView.State(
            pieces.map { it.toPieceState() }.toSet(),
            arrows.map { it.toArrowState() }.toSet()
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ChessBoardView.State.EMPTY
    )

    fun showRandomBoard() {
        board.generateRandomPosition()
    }

    internal class Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val api = StockfishApi("http://10.0.2.2:8080")
            val engine = Engine(api)
            return MainViewModel(engine, Board()) as T
        }
    }
}

private fun Board.Arrow.toArrowState() = ChessBoardView.State.Arrow(
    this.start,
    this.end,
    Color.parseColor("#648EBA")
)

private fun Map.Entry<Board.Square, Board.Piece>.toPieceState() = ChessBoardView.State.Piece(
    this.key.notation,
    if (this.value is Board.Piece.WhitePawn) Color.WHITE else Color.BLACK
)