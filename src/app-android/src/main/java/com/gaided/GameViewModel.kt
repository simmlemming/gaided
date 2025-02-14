package com.gaided

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.gaided.app.common.util.toPiece
import com.gaided.app.common.viewmodel.ChessViewModel
import com.gaided.board.stockfish.Board
import com.gaided.chessui.model.ChessBoardViewState
import com.gaided.chessui.model.EvaluationViewState
import com.gaided.chessui.model.PlayerViewState
import com.gaided.engine.Engine
import com.gaided.engine.openai.createOpenAiEngine
import com.gaided.engine.stockfish.createStockfishEngine
import com.gaided.chessgame.ChessGame
import com.gaided.model.FenNotation
import com.gaided.model.MoveNotation
import com.gaided.model.SquareNotation
import com.gaided.util.sorted
import com.gaided.util.toLastMoveSquares
import com.gaided.util.toLastTopMoveArrows
import com.gaided.util.toNextMovePlayer
import com.gaided.util.toPlayerState
import com.gaided.util.toTopMoveArrows
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.shareIn
import kotlin.reflect.KClass

class GameViewModel(private val game: ChessGame) : ChessViewModel(game) {

    @Suppress("OPT_IN_USAGE")
    private val topMoves = game.position
        .flatMapLatest { position -> game.getTopMoves(position) }
        .shareIn(safeViewModelScope, SharingStarted.WhileSubscribed(), 1)

    @Suppress("OPT_IN_USAGE")
    private val oldTopMoves: SharedFlow<Pair<ChessGame.Player, List<Engine.TopMove>>> = game.history
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
        toPlayerState(ChessGame.Player.White, position, topMoves.moves, topMoves.inProgress)
    }.stateInThis(PlayerViewState.EMPTY)

    val playerBlack = combine(game.started, game.position, topMoves) { started, position, topMoves ->
        if (!started) return@combine PlayerViewState.EMPTY
        toPlayerState(ChessGame.Player.Black, position, topMoves.moves, topMoves.inProgress)
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

    override val position = game.position
        .stateInThis(FenNotation.START_POSITION, SharingStarted.Eagerly)

    fun start() {
        game.start()
    }

    override fun onSquareClick(square: SquareNotation) = launch {
        when {
            selectedSquare.value == null && square.hasArrow() -> {
                onArrowClick(square)
            }

            else -> super.onSquareClick(square)
        }
    }

    private fun SquareNotation.hasArrow() =
        topMoveStartSquares.value.getMovesFromSquare(this).isNotEmpty()

    private suspend fun onArrowClick(square: SquareNotation) {
        val topMovesFromSquare = topMoveStartSquares.value.getMovesFromSquare(square)

        // One arrow from clicked square
        if (topMovesFromSquare.size == 1) {
            topMovesFromSquare.toList()[0].second.invoke(game, pendingMove)
        } else {
            selectedSquare.value = square
        }
    }

    private fun Set<ChessGame.HalfMove>.toOneBeforeLastTopMoves(): Flow<Pair<ChessGame.Player, List<Engine.TopMove>>> {
        return when (val position = this.oneBeforeLastHalfMoveOrNull()) {
            null -> flowOf(ChessGame.Player.White to emptyList())
            else -> game.getTopMoves(position.positionAfterMove).map {
                position.positionAfterMove.toNextMovePlayer() to it.moves
            }
        }
    }

    private fun Set<ChessGame.HalfMove>.toOneBeforeLastPosition(): FenNotation =
        oneBeforeLastHalfMoveOrNull()?.positionAfterMove ?: FenNotation.START_POSITION

    class Factory(private val config: Config) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            val board = Board(url = config.remoteBoardUrl)
            val stockfishEngine = createStockfishEngine(url = config.stockfishEngineUrl)
            val openAiEngine = createOpenAiEngine(apiKey = config.openAiApiKey)

            val game = ChessGame(board, listOf(openAiEngine, stockfishEngine))
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


private typealias MakeMoveAction = suspend (ChessGame, MutableStateFlow<MoveNotation?>) -> Unit

private fun Set<ChessGame.HalfMove>.oneBeforeLastHalfMoveOrNull(): ChessGame.HalfMove? {
    if (this.isEmpty()) {
        return null
    }

    if (this.size == 1) {
        return ChessGame.HalfMove(0, "", ChessGame.Player.White, FenNotation.START_POSITION)
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
