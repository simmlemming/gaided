package com.gaided.fortress.app.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.gaided.app.common.util.toPiece
import com.gaided.app.common.viewmodel.ChessViewModel
import com.gaided.chessui.model.ChessBoardViewState
import com.gaided.fortress.game.FortressGame
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.reflect.KClass

internal class FortressGameViewModel(private val game: FortressGame) : ChessViewModel() {

    val chessBoardViewState: Flow<ChessBoardViewState> = game.position.map { position ->
        ChessBoardViewState(
            pieces = position.allPieces()
                .map { it.toPiece(null, null) }
                .toSet()
        )
    }

    internal class Factory : ViewModelProvider.Factory {
        @Suppress("kotlin:S6530", "UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            val game = FortressGame()
            return FortressGameViewModel(game) as T
        }
    }
}