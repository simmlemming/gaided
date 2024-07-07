package com.gaided.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.gaided.engine.Engine
import com.gaided.engine.createOpenAiEngine
import com.gaided.engine.stockfish.createStockfishEngine
import com.gaided.game.ui.model.ChessBoardViewState
import com.gaided.game.ui.model.EvaluationViewState
import com.gaided.game.ui.model.PlayerViewState
import com.gaided.game.util.toLastMoveSquares
import com.gaided.game.util.toLastTopMoveArrows
import com.gaided.game.util.toNextMovePlayer
import com.gaided.game.util.toPiece
import com.gaided.game.util.toPlayerState
import com.gaided.game.util.toTopMoveArrows
import com.gaided.logger.Logger
import com.gaided.model.FenNotation
import com.gaided.model.MoveNotation
import com.gaided.model.PieceNotation
import com.gaided.model.SquareNotation
import com.gaided.board.stockfish.Board
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlin.reflect.KClass

class GameViewModel(private val game: Game) : ViewModel() {
    private val exceptionsHandler = CoroutineExceptionHandler { _, e ->
        Logger.e("", e)
        _userMessage.value = e.message ?: "Error"
    }

    private val safeViewModelScope: CoroutineScope = viewModelScope + exceptionsHandler
    private val selectedSquare = MutableStateFlow<SquareNotation?>(null)
    private val pendingMove = MutableStateFlow<MoveNotation?>(null)

    @Suppress("OPT_IN_USAGE")
    private val topMoves = game.position
        .flatMapLatest { position -> game.getTopMoves(position) }
        .shareIn(safeViewModelScope, SharingStarted.WhileSubscribed(), 1)

    @Suppress("OPT_IN_USAGE")
    private val oldTopMoves: SharedFlow<Pair<Game.Player, List<Engine.TopMove>>> = game.history
        .flatMapLatest { it.toOneBeforeLastTopMoves() }
        .shareIn(safeViewModelScope, SharingStarted.WhileSubscribed(), 1)

    @Suppress("OPT_IN_USAGE")
    private val oldPosition: StateFlow<FenNotation> = game.history
        .mapLatest { it.toOneBeforeLastPosition() }
        .stateInThis(FenNotation.START_POSITION)

    val board =
        combine(
            game.position,
            topMoves,
            oldTopMoves,
            game.history,
            selectedSquare,
            pendingMove
        ) { position, topMoves, oldTopMoves, history, selectedSquare, pendingMove ->
            ChessBoardViewState(
                pieces = position
                    .allPieces()
                    .let { if (pendingMove == null) it else it.move(pendingMove) }
                    .map { it.toPiece(selectedSquare, null) }
                    .toSet(),
                arrows = toTopMoveArrows(topMoves.moves, selectedSquare, pendingMove) +
                        toLastTopMoveArrows(oldTopMoves.first, oldTopMoves.second),
                overlaySquares = pendingMove?.toLastMoveSquares() ?: history.toLastMoveSquares()
            )
        }.stateInThis(ChessBoardViewState.EMPTY)

    val playerWhite = combine(game.started, game.position, topMoves) { started, position, topMoves ->
        if (!started) return@combine PlayerViewState.EMPTY
        toPlayerState(Game.Player.White, position, topMoves.moves, topMoves.inProgress)
    }.stateInThis(PlayerViewState.EMPTY)

    val playerBlack = combine(game.started, game.position, topMoves) { started, position, topMoves ->
        if (!started) return@combine PlayerViewState.EMPTY
        toPlayerState(Game.Player.Black, position, topMoves.moves, topMoves.inProgress)
    }.stateInThis(PlayerViewState.EMPTY)

    val evaluation =
        combine(game.started, game.position, oldPosition, game.evaluation) { started, position, oldPosition, evaluation ->
            if (!started) return@combine EvaluationViewState.INITIAL

            val positionEvaluation = evaluation[position]?.value
            val oldPositionEvaluation = evaluation[oldPosition]?.value
            val isLoading = positionEvaluation == null

            EvaluationViewState(positionEvaluation ?: oldPositionEvaluation ?: 0, isLoading)
        }
            .distinctUntilChanged()
            .stateInThis(EvaluationViewState.INITIAL)

    private val topMoveStartSquares = combine(game.position, topMoves) { position, topMoves ->
        topMoves.moves.associate { topMove -> topMove.move to topMove.toMakeMoveAction(position) }
    }.stateInThis(emptyMap(), SharingStarted.Eagerly)

    private val position = game.position
        .stateInThis(FenNotation.START_POSITION, SharingStarted.Eagerly)

    private val _userMessage = MutableStateFlow("")
    val userMessage = _userMessage.asStateFlow()

    fun start() {
        game.start()
    }

    @Deprecated("Use onSquareClick().")
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

    fun onUserMessageShown() {
        _userMessage.value = ""
    }

    private fun launch(block: suspend CoroutineScope.() -> Unit) =
        safeViewModelScope.launch(block = block)

    private fun <T> Flow<T>.stateInThis(
        initialValue: T,
        started: SharingStarted = SharingStarted.WhileSubscribed(5000)
    ): StateFlow<T> = stateIn(safeViewModelScope, started, initialValue)

    private fun Set<Game.HalfMove>.toOneBeforeLastTopMoves(): Flow<Pair<Game.Player, List<Engine.TopMove>>> {
        return when (val position = this.oneBeforeLastHalfMoveOrNull()) {
            null -> flowOf(Game.Player.White to emptyList())
            else -> game.getTopMoves(position.positionAfterMove).map {
                position.positionAfterMove.toNextMovePlayer() to it.moves
            }
        }
    }

    private fun Set<Game.HalfMove>.toOneBeforeLastPosition(): FenNotation =
        oneBeforeLastHalfMoveOrNull()?.positionAfterMove ?: FenNotation.START_POSITION

    class Factory(private val config: Config) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            val board = Board(url = config.remoteBoardUrl)
            val stockfishEngine = createStockfishEngine(url = config.stockfishEngineUrl)
            val openAiEngine = createOpenAiEngine(apiKey = config.openAiApiKey)

            val game = Game(board, listOf(openAiEngine, stockfishEngine))
            return GameViewModel(game) as T
        }

        data class Config(
            val remoteBoardUrl: String,
            val stockfishEngineUrl: String,
            val openAiApiKey: String
        )
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

private fun Set<Game.HalfMove>.oneBeforeLastHalfMoveOrNull(): Game.HalfMove? {
    if (this.isEmpty()) {
        return null
    }

    if (this.size == 1) {
        return Game.HalfMove(0, "", Game.Player.White, FenNotation.START_POSITION)
    }

    val sortedHistory = this.sorted()
    return sortedHistory[sortedHistory.lastIndex - 1]
}

private inline fun <T1, T2, T3, T4, T5, T6, R> combine(
    flow: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    crossinline transform: suspend (T1, T2, T3, T4, T5, T6) -> R
): Flow<R> {
    return combine(flow, flow2, flow3, flow4, flow5, flow6) { args: Array<*> ->
        @Suppress("UNCHECKED_CAST")
        transform(
            args[0] as T1,
            args[1] as T2,
            args[2] as T3,
            args[3] as T4,
            args[4] as T5,
            args[5] as T6,
        )
    }
}

private fun Map<SquareNotation, PieceNotation>.move(move: MoveNotation): Map<SquareNotation, PieceNotation> {
    return this.toMutableMap().let {
        if (!it.containsKey(move.take(2))) {
            return@let it
        }

        it[move.takeLast(2)] = checkNotNull(it.remove(move.take(2)))
        it.toMap()
    }
}
