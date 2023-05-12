package com.gaided

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gaided.domain.Board
import com.gaided.domain.MoveNotation
import com.gaided.util.toArrowViewState
import com.gaided.util.toPieceViewState
import com.gaided.util.toPlayerViewState
import com.gaided.view.chessboard.ChessBoardView
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

internal class GameViewModel(private val game: Game) : ViewModel() {
    val board = combine(game.board.pieces, game.board.arrows) { pieces, arrows ->
        ChessBoardView.State(
            pieces.map { it.toPieceViewState() }.toSet(),
            arrows.map { it.toArrowViewState() }.toSet()
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ChessBoardView.State.EMPTY
    )

    private val _userMessage = MutableStateFlow("")
    val userMessage = _userMessage.asStateFlow()

    val playerWhite = combine(game.board.pieces, game.state) { pieces, state ->
        state.toPlayerViewState(pieces, Game.Player.White)
    }

    val playerBlack = combine(game.board.pieces, game.state) { pieces, state ->
        state.toPlayerViewState(pieces, Game.Player.Black)
    }

    fun start() = launch {
        game.start()
    }

    fun move(move: MoveNotation) = launch {
        game.move(Game.Player.White, move)
    }

    fun onMoveClick(player: Game.Player, move: MoveNotation) = launch {
        game.selectTopMove(player, move)
    }

    internal fun onUserMessageShown() {
        _userMessage.value = ""
    }

    private fun launch(block: suspend CoroutineScope.() -> Unit) =
        safeViewModelScope.launch(block = block)

    private val exceptionsHandler = CoroutineExceptionHandler { _, e ->
        _userMessage.value = e.message ?: "Error"
    }

    private val safeViewModelScope: CoroutineScope = viewModelScope + exceptionsHandler

    internal class Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val api = com.gaided.domain.api.StockfishApi("http://10.0.2.2:8080")
            val engine = com.gaided.domain.Engine(api)
            val game = Game(engine, Board())
            return GameViewModel(game) as T
        }
    }
}