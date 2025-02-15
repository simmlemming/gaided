package com.gaided.fortress.app.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.gaided.app.common.util.toPiece
import com.gaided.app.common.viewmodel.ChessViewModel
import com.gaided.board.stockfish.Board
import com.gaided.chessgame.ChessGame
import com.gaided.chessui.model.ChessBoardViewState
import com.gaided.engine.stockfish.createStockfishEngine
import com.gaided.model.FenNotation
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlin.reflect.KClass

internal class FortressViewModel(private val game: ChessGame) : ChessViewModel(game) {
    override val position: StateFlow<FenNotation> =
        game.position.stateInThis(FenNotation.START_POSITION)

    val chessBoardViewState: StateFlow<ChessBoardViewState> =
        combine(position, selectedSquare, pendingMove) { position, selectedSquare, pendingMove ->
            ChessBoardViewState(
                pieces = position
                    .allPieces()
                    .let { if (pendingMove == null) it else it.move(pendingMove) }
                    .map { it.toPiece(selectedSquare, null) }
                    .toSet(),
            )
        }.stateInThis(ChessBoardViewState.EMPTY)

    internal class Factory(
        private val remoteBoardUrl: String = "http://10.0.2.2:8080",
        private val stockfishEngineUrl: String = "http://10.0.2.2:8081",
    ) : ViewModelProvider.Factory {
        @Suppress("kotlin:S6530", "UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            val game = ChessGame(
                board = Board(url = remoteBoardUrl),
                engines = listOf(createStockfishEngine(url = stockfishEngineUrl))
            )
            return FortressViewModel(game) as T
        }
    }
}