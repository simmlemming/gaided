package com.gaided.fortress.app.android.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Person
import com.gaided.app.common.util.toNextMovePlayerColor
import com.gaided.chessgame.ChessGame
import com.gaided.fortress.app.android.FortressViewModel
import com.gaided.fortress.app.android.ui.FortressPlayerViewState
import com.gaided.model.FenNotation

internal fun ChessGame.State.toFortressPLayerViewState(
    playerInfo: FortressViewModel.PlayerInfo
) = FortressPlayerViewState(
    name = playerInfo.name,
    icon = when (playerInfo.type) {
        is FortressViewModel.PlayerType.Human -> Icons.Default.Person
        else -> Icons.Default.Computer
    },
    progressVisible = (this is ChessGame.State.WaitingForMove && this.nextMovePlayerColor == playerInfo.color)
)
