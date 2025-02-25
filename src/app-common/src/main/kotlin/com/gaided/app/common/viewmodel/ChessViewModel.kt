package com.gaided.app.common.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gaided.app.common.util.toLastMoveSquares
import com.gaided.app.common.util.toPiece
import com.gaided.chessgame.ChessGame
import com.gaided.chessgame.ChessGame.Player
import com.gaided.chessui.model.ChessBoardViewState
import com.gaided.logger.Logger
import com.gaided.model.FenNotation
import com.gaided.model.MoveNotation
import com.gaided.model.PieceNotation
import com.gaided.model.SquareNotation
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

abstract class ChessViewModel(
    private val game: ChessGame
) : ViewModel() {
    private val exceptionsHandler = CoroutineExceptionHandler { _, e ->
        Logger.e("", e)
        _userMessage.value = e.message ?: "Error"
    }
    protected val safeViewModelScope: CoroutineScope = viewModelScope + exceptionsHandler

    protected val selectedSquare = MutableStateFlow<SquareNotation?>(null)
    protected val pendingMove = MutableStateFlow<MoveNotation?>(null)
    protected abstract val position: StateFlow<FenNotation>

    private val _userMessage = MutableStateFlow("")
    val userMessage = _userMessage.asStateFlow()

    val board =
        combine(
            game.position,
            game.history,
            selectedSquare,
            pendingMove
        ) { position, history, selectedSquare, pendingMove ->
            ChessBoardViewState(
                pieces = position
                    .allPieces()
                    .let { if (pendingMove == null) it else it.move(pendingMove) }
                    .map { it.toPiece(selectedSquare, null) }
                    .toSet(),
                overlaySquares = pendingMove?.toLastMoveSquares() ?: history.toLastMoveSquares()
            )
        }.stateInThis(ChessBoardViewState.EMPTY)

    fun start() {
        game.start()
    }

    fun startWithPlayers(playerWhite: Player, playerBlack: Player) = launch {
        game.start(playerWhite, playerBlack)
    }

    fun onUserMessageShown() {
        _userMessage.value = ""
    }

    open fun onSquareClick(square: SquareNotation) = launch {
        when {
            selectedSquare.value == square -> {
                selectedSquare.value = null
            }

            selectedSquare.value == null && position.value.allPieces().containsKey(square) -> {
                selectedSquare.value = square
            }

            selectedSquare.value != null && selectedSquare.value != square -> {
                val move = "${selectedSquare.value}$square"
                pendingMove.value = move
                if (game.isMoveCorrect(move)) {
                    game.move(move)
                }
                selectedSquare.value = null
                pendingMove.value = null
            }
        }
    }

    fun onSquareLongClick(square: SquareNotation) {
        selectedSquare.value = square
    }

    protected fun launch(block: suspend CoroutineScope.() -> Unit) =
        safeViewModelScope.launch(block = block)

    protected fun <T> Flow<T>.stateInThis(
        initialValue: T,
        started: SharingStarted = SharingStarted.WhileSubscribed(5000)
    ): StateFlow<T> = stateIn(safeViewModelScope, started, initialValue)

    protected fun Map<SquareNotation, PieceNotation>.move(move: MoveNotation): Map<SquareNotation, PieceNotation> {
        return this.toMutableMap().let {
            if (!it.containsKey(move.take(2))) {
                return@let it
            }

            it[move.takeLast(2)] = checkNotNull(it.remove(move.take(2)))
            it.toMap()
        }
    }

}