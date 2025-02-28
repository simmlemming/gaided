package com.gaided.fortress.app.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.gaided.app.common.viewmodel.ChessViewModel
import com.gaided.board.stockfish.Board
import com.gaided.chessgame.ChessGame
import com.gaided.engine.Engine
import com.gaided.engine.stockfish.createStockfishEngine
import com.gaided.fortress.app.android.ui.FortressPlayerViewState
import com.gaided.fortress.app.android.util.toFortressPLayerViewState
import com.gaided.model.FenNotation
import com.gaided.model.MoveNotation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlin.reflect.KClass

internal class FortressViewModel(
    private val game: ChessGame,
    private val createPlayer: (PlayerType, ChessGame.Player.Color) -> ChessGame.Player,
) : ChessViewModel(game) {

    private val players = MutableStateFlow<Map<ChessGame.Player.Color, PlayerInfo>>(emptyMap())

    val playerWhite: StateFlow<FortressPlayerViewState> = combine(game.state, players) { state, players ->
        val player = players[ChessGame.Player.Color.White] ?: return@combine FortressPlayerViewState.EMPTY
        state.toFortressPLayerViewState(player)
    }.stateInThis(FortressPlayerViewState.EMPTY)

    val playerBlack: StateFlow<FortressPlayerViewState> = combine(game.state, players) { state, players ->
        val playerInfo = players[ChessGame.Player.Color.Black] ?: return@combine FortressPlayerViewState.EMPTY
        state.toFortressPLayerViewState(playerInfo)
    }.stateInThis(FortressPlayerViewState.EMPTY)

    fun startFortressGame(playerTypeWhite: PlayerType, playerTypeBlack: PlayerType) {
        val gamePlayerWhite = createPlayer(playerTypeWhite, ChessGame.Player.Color.White)
        val gamePlayerBlack = createPlayer(playerTypeBlack, ChessGame.Player.Color.Black)

        players.value = mapOf(
            ChessGame.Player.Color.White to PlayerInfo(
                name = gamePlayerWhite.name,
                type = playerTypeWhite,
                color = ChessGame.Player.Color.White,
            ),
            ChessGame.Player.Color.Black to PlayerInfo(
                name = gamePlayerBlack.name,
                type = playerTypeBlack,
                color = ChessGame.Player.Color.Black,
            )
        )

        startWithPlayers(
            playerWhite = gamePlayerWhite,
            playerBlack = gamePlayerBlack,
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

            val createPlayer: (PlayerType, ChessGame.Player.Color) -> ChessGame.Player = { player, color ->
                when (player) {
                    PlayerType.Stockfish -> Bot(color = color, engine = stockfishEngine)
                    else -> throw IllegalArgumentException("$player is not supported.")
                }
            }

            return FortressViewModel(game, createPlayer) as T
        }
    }

    sealed class PlayerType {
        data class Human(val name: String) : PlayerType()
        data object Stockfish : PlayerType()
    }

    internal data class PlayerInfo(
        val type: PlayerType,
        val name: String,
        val color: ChessGame.Player.Color,
    )
}

// TODO: Move `Bot` to a proper place.
class Bot(
    override val color: ChessGame.Player.Color,
    private val engine: Engine
) : ChessGame.Player {
    override val name = engine.name

    override suspend fun getMove(position: FenNotation): MoveNotation? {
        return engine.getTopMoves(
            position = position,
            numberOfMoves = 1,
        ).firstOrNull()?.move
    }
}
