package com.gaided.fortress.app.android.util

import com.gaided.app.common.util.toNextMovePlayerColor
import com.gaided.chessgame.ChessGame
import com.gaided.fortress.app.android.ui.FortressPlayerViewState
import com.gaided.model.FenNotation

internal fun FenNotation.toFortressPLayerViewState(color: ChessGame.Player.Color) =
    FortressPlayerViewState(progressVisible = this.toNextMovePlayerColor() == color)