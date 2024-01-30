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
import com.gaided.util.toLastMoveSquares
import com.gaided.util.toLastTopMoveArrows
import com.gaided.util.toNextMovePlayer
import com.gaided.util.toPiece
import com.gaided.util.toPlayerState
import com.gaided.util.toTopMoveArrows
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.shareIn
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

    @Suppress("OPT_IN_USAGE")
    private val hotTopMoves = game.position
        .flatMapLatest { position -> game.getTopMoves(position) }
        .shareIn(safeViewModelScope, SharingStarted.WhileSubscribed(), 1)

    val board =
        combine(
            game.position,
            hotTopMoves,
            game.history,
            selectedSquare,
            pendingMove
        ) { position, topMoves, history, selectedSquare, pendingMove ->
            ChessBoardView.State(
                pieces = position
                    .allPieces()
                    .let { if (pendingMove == null) it else it.move(pendingMove) }
                    .map { it.toPiece(selectedSquare, null) }
                    .toSet(),
                arrows = toTopMoveArrows(topMoves, selectedSquare, pendingMove) +
                        toLastTopMoveArrows(topMoves),
                overlaySquares = pendingMove?.toLastMoveSquares() ?: history.toLastMoveSquares()
            )
        }.stateInThis(
            ChessBoardView.State.EMPTY
        )

    private fun Map<SquareNotation, PieceNotation>.move(move: MoveNotation): Map<SquareNotation, PieceNotation> {
        return this.toMutableMap().let {
            if (!it.containsKey(move.take(2))) {
                return@let it
            }

            it[move.takeLast(2)] = checkNotNull(it.remove(move.take(2)))
            it.toMap()
        }
    }

    val playerWhite = combine(game.started, game.position, hotTopMoves, pendingMove) { started, position, topMoves, pendingMove ->
        if (!started) return@combine PlayerView.State.EMPTY
        toPlayerState(Game.Player.White, position, topMoves, pendingMove)
    }.stateInThis(PlayerView.State.EMPTY)

    val playerBlack = combine(game.started, game.position, hotTopMoves, pendingMove) { started, position, topMoves, pendingMove ->
        if (!started) return@combine PlayerView.State.EMPTY
        toPlayerState(Game.Player.Black, position, topMoves, pendingMove)
    }.stateInThis(PlayerView.State.EMPTY)

    val evaluation = combine(game.started, game.position, game.evaluation) { started, position, evaluation ->
        if (!started) return@combine EvaluationView.State.INITIAL
        val e = evaluation[position] ?: return@combine EvaluationView.State.LOADING
        EvaluationView.State(e.value, false)
    }.stateInThis(EvaluationView.State.INITIAL)

    private val topMoveStartSquares = combine(game.position, hotTopMoves) { position, topMoves ->
        topMoves
            .associate { topMove -> topMove.move to topMove.toMakeMoveAction(position) }
    }.stateInThis(emptyMap(), SharingStarted.Eagerly)

    private val position = game.position
        .stateInThis(FenNotation.START_POSITION, SharingStarted.Eagerly)

    private val _userMessage = MutableStateFlow("")
    val userMessage = _userMessage.asStateFlow()

    fun start() {
        game.start()
    }

    fun onMoveClick(player: Game.Player, move: MoveNotation) = launch {
        game.move(move, player)
    }

    fun onSquareClick(square: SquareNotation) = launch {
        when {
            selectedSquare.value == null && topMoveStartSquares.value.getMovesFromSquare(square).isNotEmpty() -> {
                onArrowClick(square)
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

    private suspend fun onArrowClick(square: SquareNotation) {
        val topMovesFromSquare = topMoveStartSquares.value.getMovesFromSquare(square)

        // One arrow from clicked square
        if (topMovesFromSquare.size == 1) {
            topMovesFromSquare.toList()[0].second.invoke(game, pendingMove)
        } else {
            selectedSquare.value = square
        }
    }

    fun onSquareLongClick(square: SquareNotation) {
        selectedSquare.value = square
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

private fun Engine.TopMove.toMakeMoveAction(position: FenNotation): MakeMoveAction {
    return { game, pendingMove ->
        pendingMove.value = move
        game.move(move, position.toNextMovePlayer())
        pendingMove.value = null
    }
}

private fun Map<MoveNotation, MakeMoveAction>.getMovesFromSquare(square: SquareNotation) =
    filter { it.key.take(2) == square }


private typealias MakeMoveAction = suspend (Game, MutableStateFlow<MoveNotation?>) -> Unit
