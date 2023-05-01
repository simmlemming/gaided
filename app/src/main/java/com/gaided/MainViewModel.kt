package com.gaided

import android.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gaided.api.StockfishApi
import com.gaided.domain.Board
import com.gaided.domain.Game
import com.gaided.view.chessboard.ChessBoardView
import com.gaided.view.chessboard.SquareNotation
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

internal class MainViewModel(
    private val game: Game
) : ViewModel() {
    val boardState = combine(game.board.pieces, game.board.arrows) { pieces, arrows ->
        ChessBoardView.State(
            pieces.map { it.toPieceState() }.toSet(),
            arrows.map { it.toArrowState() }.toSet()
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ChessBoardView.State.EMPTY
    )

    fun move(from: SquareNotation, to: SquareNotation) {
        game.move(from, to)
    }

    internal class Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val api = StockfishApi("http://10.0.2.2:8080")
            val engine = Engine(api)
            val game = Game(engine, Board())
            return MainViewModel(game) as T
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