package com.gaided

import android.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gaided.domain.Board
import com.gaided.domain.SquareNotation
import com.gaided.view.chessboard.ChessBoardView
import com.gaided.view.player.PlayerView
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

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

    private val _userMessage = MutableStateFlow("")
    val userMessage = _userMessage.asStateFlow()

    private val _player1 = MutableStateFlow(PlayerView.State.EMPTY)
    val player1 = _player1.asStateFlow()

    private val _player2 = MutableStateFlow(PlayerView.State.EMPTY)
    val player2 = _player2.asStateFlow()

    fun start() = launch {
        game.start()
    }

    fun move(from: SquareNotation, to: SquareNotation) = launch {
        game.move(Game.Player.White, from, to)
    }

    fun onPlayer1MoveSelected(id: Int) {
    }

    fun onPlayer2MoveSelected(id: Int) {
    }

    private fun launch(block: suspend CoroutineScope.() -> Unit) =
        safeViewModelScope.launch(block = block)

    private val exceptionsHandler = CoroutineExceptionHandler { _, e ->
        _userMessage.value = e.message ?: "Error"
    }

    private val safeViewModelScope: CoroutineScope = viewModelScope + exceptionsHandler

    internal fun userMessageShown() {
        _userMessage.value = ""
    }

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