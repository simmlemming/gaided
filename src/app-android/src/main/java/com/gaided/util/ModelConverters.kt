package com.gaided.util

import com.gaided.app.common.util.toNextMovePlayerColor
import com.gaided.chessgame.ChessGame
import com.gaided.chessui.model.ChessBoardViewState
import com.gaided.chessui.model.PlayerViewState
import com.gaided.engine.Engine
import com.gaided.engine.openai.OPEN_AI_ENGINE_NAME
import com.gaided.model.FenNotation
import com.gaided.model.MoveNotation
import com.gaided.model.SquareNotation

internal fun toLastTopMoveArrows(player: ChessGame.Player.Color, topMoves: List<Engine.TopMove>): Set<ChessBoardViewState.Arrow> {
    val comparator = Comparator<Engine.TopMove> { o1, o2 ->
        if (player == ChessGame.Player.Color.White) {
            o2.centipawn!! - o1.centipawn!!
        } else {
            o1.centipawn!! - o2.centipawn!!
        }
    }

    val movesWithEvaluation = topMoves
        .filter { it.centipawn != null }
        .sortedWith(comparator)

    val movesWithoutEvaluation = topMoves
        .filter { it.centipawn == null }

    return (movesWithEvaluation + movesWithoutEvaluation)
        .mapIndexed { index, move ->
            val color =
                if (move.centipawn == null) ChessBoardViewState.Arrow.COLOR_SUGGESTION else ChessBoardViewState.Arrow.colorByTopMoveIndex(
                    index
                )
            move.toArrow(color)
        }
        .toSet()
}

internal fun toTopMoveArrows(
    topMoves: List<Engine.TopMove>,
    selectedSquare: SquareNotation?,
    pendingMove: MoveNotation?
): Set<ChessBoardViewState.Arrow> {
    if (pendingMove != null) {
        return emptySet()
    }

    return topMoves
        .filter { selectedSquare == null || it.move.from == selectedSquare }
        .map { it.toArrow(ChessBoardViewState.Arrow.COLOR_SUGGESTION) }
        .toSet()
}

internal fun toPlayerState(
    player: ChessGame.Player.Color,
    position: FenNotation,
    topMoves: List<Engine.TopMove>,
    isLoading: Boolean
): PlayerViewState {
    val nextMovePlayer = position.toNextMovePlayerColor()

    return when {
        nextMovePlayer == ChessGame.Player.Color.None ->
            PlayerViewState.EMPTY

        nextMovePlayer == player ->
            toPlayerViewState(position, isLoading)

        nextMovePlayer == player ->
            PlayerViewState.EMPTY

        nextMovePlayer != player ->
            PlayerViewState.OPPONENT_MOVE

        nextMovePlayer != player ->
            PlayerViewState.EMPTY.copy(progressVisible = true)

        else -> PlayerViewState.EMPTY
    }
}

private fun toPlayerViewState(position: FenNotation, isLoading: Boolean): PlayerViewState {
    return PlayerViewState(
        progressVisible = isLoading,
        movesStats = emptyList()
    )
}

internal fun Engine.TopMove.toArrow(color: Int) = ChessBoardViewState.Arrow(
    start = this.move.from,
    end = this.move.to,
    color = color,
    strong = (this.source != OPEN_AI_ENGINE_NAME)
)
