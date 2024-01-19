package com.gaided

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gaided.domain.MoveNotation
import com.gaided.domain.SquareNotation
import com.gaided.util.toArrow
import com.gaided.util.toPiece
import com.gaided.util.toPlayerState
import com.gaided.view.chessboard.ChessBoardView
import com.gaided.view.player.PlayerView
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

internal class GameViewModel(private val game: Game) : ViewModel() {
    private val exceptionsHandler = CoroutineExceptionHandler { _, e ->
        Log.e("Gaided", "", e)
        _userMessage.value = e.message ?: "Error"
    }

    private val safeViewModelScope: CoroutineScope = viewModelScope + exceptionsHandler

    val board = combine(game.position, game.topMoves, game.history) { position, topMoves, history ->
        ChessBoardView.State(
            pieces = position.allPieces().map { it.toPiece() }.toSet(),
            arrows = topMoves[position].orEmpty().map { it.toArrow() }.toSet(),
            overlaySquares = emptySet()
        )
    }.stateInThis(ChessBoardView.State.EMPTY)

    val playerWhite = combine(game.position, game.topMoves) { position, topMoves ->
        toPlayerState(Game.Player.White, position, topMoves)
    }.stateInThis(PlayerView.State.EMPTY)

    val playerBlack = combine(game.position, game.topMoves) { position, topMoves ->
        toPlayerState(Game.Player.Black, position, topMoves)
    }.stateInThis(PlayerView.State.EMPTY)

    private val _userMessage = MutableStateFlow("")
    val userMessage = _userMessage.asStateFlow()

    fun start() = launch {
        game.start()
    }

    fun move(move: MoveNotation) = launch {
        game.move(Game.Player.White, move)
    }

    fun onMoveClick(player: Game.Player, move: MoveNotation) = launch {
        game.move(player, move)
    }

    fun onSquareClick(square: SquareNotation) {

    }

    internal fun onUserMessageShown() {
        _userMessage.value = ""
    }

    private fun launch(block: suspend CoroutineScope.() -> Unit) =
        safeViewModelScope.launch(block = block)

    private fun <T> Flow<T>.stateInThis(initialValue: T): StateFlow<T> = stateIn(
        safeViewModelScope, SharingStarted.WhileSubscribed(5000), initialValue
    )

    internal class Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val api = com.gaided.domain.api.StockfishApi("http://10.0.2.2:8080")
            val engine = com.gaided.domain.Engine(api)
            val game = Game(engine)
            return GameViewModel(game) as T
        }
    }
}