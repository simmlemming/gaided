package com.gaided

import android.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gaided.domain.Board
import com.gaided.domain.SquareNotation
import com.gaided.view.chessboard.ChessBoardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class GameViewModel(private val game: Game) : ViewModel() {
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

    fun start() = launch {
        game.start()
    }

    fun move(from: SquareNotation, to: SquareNotation) = launch {
        game.move(from, to)
    }

    private fun launch(block: suspend CoroutineScope.() -> Unit) =
        viewModelScope.launch(block = block)

    internal class Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val api = com.gaided.domain.api.StockfishApi("http://10.0.2.2:8080")
            val engine = com.gaided.domain.Engine(api)
            val game = Game(engine, Board())
            return GameViewModel(game) as T
        }
    }
}

private fun Board.Arrow.toArrowState() = ChessBoardView.State.Arrow(
    this.start,
    this.end,
    Color.parseColor("#648EBA")
)

private fun Map.Entry<SquareNotation, Board.Piece>.toPieceState() = ChessBoardView.State.Piece(
    this.key,
    if (this.value.isBlack) Color.BLACK else Color.WHITE
)