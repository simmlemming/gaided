package com.gaided

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gaided.domain.Engine
import com.gaided.domain.FenNotation
import com.gaided.domain.MoveNotation
import com.gaided.domain.PieceNotation
import com.gaided.domain.SquareNotation
import com.gaided.domain.api.StockfishApi
import com.gaided.util.toArrow
import com.gaided.util.toLastMoveSquares
import com.gaided.util.toNextMovePlayer
import com.gaided.util.toPiece
import com.gaided.util.toPieces
import com.gaided.util.toPlayerState
import com.gaided.view.chessboard.ChessBoardView
import com.gaided.view.evaluation.EvaluationView
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
    private val selectedSquare = MutableStateFlow<SquareNotation?>(null)
    private val pendingMove = MutableStateFlow<MoveNotation?>(null)
    private var gameStarted: Boolean = false

    val board =
        combine(
            game.position,
            game.topMoves,
            game.history,
            selectedSquare,
            pendingMove
        ) { position, topMoves, history, selectedSquare, pendingMove ->
            ChessBoardView.State(
                pieces = position
                    .allPieces()
                    .let { if (pendingMove == null) it else it.move(pendingMove) }
                    .map { it.toPiece(selectedSquare, pendingMove) }
                    .toSet(),
                arrows = if (pendingMove == null) topMoves[position].orEmpty().map { it.toArrow() }.toSet() else emptySet(),
                overlaySquares = pendingMove?.toLastMoveSquares() ?: history.toLastMoveSquares()
            )
        }.stateInThis(
            ChessBoardView.State(
                pieces = FenNotation.START_POSITION.toPieces(),
                arrows = emptySet(),
                overlaySquares = emptySet()
            )
        )

    private fun Map<SquareNotation, PieceNotation>.move(move: MoveNotation): Map<SquareNotation, PieceNotation> {
        return this.toMutableMap().let {
            if (!it.containsKey(move.take(2))) {
                return@let it
            }

            val piece = checkNotNull(it.remove(move.take(2)))
            it[move.takeLast(2)] = piece
            it.toMap()
        }
    }

    val playerWhite = combine(game.position, game.topMoves, pendingMove) { position, topMoves, pendingMove ->
        if (!gameStarted) return@combine PlayerView.State.EMPTY
        toPlayerState(Game.Player.White, position, topMoves, pendingMove)
    }.stateInThis(PlayerView.State.EMPTY)

    val playerBlack = combine(game.position, game.topMoves, pendingMove) { position, topMoves, pendingMove ->
        if (!gameStarted) return@combine PlayerView.State.EMPTY
        toPlayerState(Game.Player.Black, position, topMoves, pendingMove)
    }.stateInThis(PlayerView.State.EMPTY)

    val evaluation = combine(game.position, game.evaluation) { position, evaluation ->
        if (!gameStarted) return@combine EvaluationView.State.INITIAL
        val e = evaluation[position] ?: return@combine EvaluationView.State.LOADING
        EvaluationView.State(e.value, false)
    }.stateInThis(EvaluationView.State.INITIAL)

    // TODO: Handle multiple top moves from the same square.
    private val topMoveStartSquares = combine(game.position, game.topMoves) { position, topMoves ->
        topMoves[position]
            .orEmpty()
            .associate { topMove -> topMove.move.take(2) to topMove.toMakeMoveAction(position) }
    }.stateInThis(emptyMap(), SharingStarted.Eagerly)

    private val position = game.position
        .stateInThis(FenNotation.START_POSITION, SharingStarted.Eagerly)

    private val _userMessage = MutableStateFlow("")
    val userMessage = _userMessage.asStateFlow()

    fun start() = launch {
        gameStarted = true
    }

    fun onMoveClick(player: Game.Player, move: MoveNotation) = launch {
        game.move(move, player)
    }

    fun onSquareClick(square: SquareNotation) = launch {
        when {
            selectedSquare.value == null && topMoveStartSquares.value[square] != null -> {
                topMoveStartSquares.value[square]!!.invoke(game, pendingMove)
            }

            selectedSquare.value == square -> {
                selectedSquare.value = null
            }

            selectedSquare.value == null && position.value.allPieces().containsKey(square) -> {
                selectedSquare.value = square
            }

            selectedSquare.value != null && selectedSquare.value != square -> {
                val move = "${selectedSquare.value}$square"
                pendingMove.value = move
                if (game.isMoveIfCorrect(move)) {
                    game.move(move)
                }
                selectedSquare.value = null
                pendingMove.value = null
            }
        }
    }

    internal fun onUserMessageShown() {
        _userMessage.value = ""
    }

    private fun launch(block: suspend CoroutineScope.() -> Unit) =
        safeViewModelScope.launch(block = block)

    private fun <T> Flow<T>.stateInThis(
        initialValue: T,
        started: SharingStarted = SharingStarted.WhileSubscribed(5000)
    ): StateFlow<T> = stateIn(safeViewModelScope, started, initialValue)

    internal class Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val api = StockfishApi("http://10.0.2.2:8080")
            val engine = Engine(api)
            val game = Game(engine)
            return GameViewModel(game) as T
        }
    }
}

private fun Engine.TopMove.toMakeMoveAction(position: FenNotation): suspend (Game, MutableStateFlow<MoveNotation?>) -> Unit {
    return { game, pendingMove ->
        pendingMove.value = move
        game.move(move, position.toNextMovePlayer())
        pendingMove.value = null
    }
}
