package com.gaided.fortress.app.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.gaided.app.common.util.toLastMoveSquares
import com.gaided.app.common.util.toPiece
import com.gaided.app.common.viewmodel.ChessViewModel
import com.gaided.board.stockfish.Board
import com.gaided.chessgame.ChessGame
import com.gaided.chessui.model.ChessBoardViewState
import com.gaided.engine.Engine
import com.gaided.engine.stockfish.createStockfishEngine
import com.gaided.model.FenNotation
import com.gaided.model.MoveNotation
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlin.reflect.KClass

internal class FortressViewModel(
    private val game: ChessGame,
    private val createPlayer: (Player, ChessGame.Player.Color) -> ChessGame.Player,
) : ChessViewModel(game) {
    override val position: StateFlow<FenNotation> =
        game.position.stateInThis(FenNotation.START_POSITION)

    val chessBoardViewState: StateFlow<ChessBoardViewState> =
        combine(position, selectedSquare, pendingMove, game.history) { position, selectedSquare, pendingMove, history ->
            ChessBoardViewState(
                pieces = position
                    .allPieces()
                    .let { if (pendingMove == null) it else it.move(pendingMove) }
                    .map { it.toPiece(selectedSquare, null) }
                    .toSet(),
                overlaySquares = pendingMove?.toLastMoveSquares() ?: history.toLastMoveSquares()
            )
        }.stateInThis(ChessBoardViewState.EMPTY)

    fun startFortressGame(playerWhite: Player, playerBlack: Player) {
        startWithPlayers(
            playerWhite = createPlayer(playerWhite, ChessGame.Player.Color.White),
            playerBlack = createPlayer(playerBlack, ChessGame.Player.Color.Black),
        )
    }

    internal class Factory(
        private val remoteBoardUrl: String = "http://10.0.2.2:8080",
        private val stockfishEngineUrl: String = "http://10.0.2.2:8081",
    ) : ViewModelProvider.Factory {
        @Suppress("kotlin:S6530", "UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            val stockfishEngine = createStockfishEngine(url = stockfishEngineUrl)

            val game = ChessGame(
                board = Board(url = remoteBoardUrl),
                engines = listOf(stockfishEngine)
            )

            val createPlayer: (Player, ChessGame.Player.Color) -> ChessGame.Player = { player, color ->
                when (player) {
                    Player.STOCKFISH -> Bot(color = color, engine = stockfishEngine)
                    else -> throw IllegalArgumentException("$player is not supported.")
                }
            }

            return FortressViewModel(game, createPlayer) as T
        }
    }

    enum class Player {
        HUMAN, STOCKFISH
    }
}

// TODO: Move `Bot` to a proper place.
class Bot(
    override val color: ChessGame.Player.Color,
    private val engine: Engine
) : ChessGame.Player {
    override suspend fun getMove(position: FenNotation): MoveNotation? {
        return engine.getTopMoves(
            position = position,
            numberOfMoves = 1,
        ).firstOrNull()?.move
    }
}
