package com.gaided.util

import com.gaided.chessui.model.ChessBoardViewState
import com.gaided.chessui.model.ChessBoardViewState.OverlaySquare
import com.gaided.chessui.model.PlayerViewState
import com.gaided.engine.Engine
import com.gaided.engine.openai.OPEN_AI_ENGINE_NAME
import com.gaided.game.Game
import com.gaided.model.FenNotation
import com.gaided.model.MoveNotation
import com.gaided.model.PieceNotation
import com.gaided.model.SquareNotation

internal fun toLastTopMoveArrows(player: Game.Player, topMoves: List<Engine.TopMove>): Set<ChessBoardViewState.Arrow> {
    val comparator = Comparator<Engine.TopMove> { o1, o2 ->
        if (player == Game.Player.White) {
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
        .filter { selectedSquare == null || it.move.take(2) == selectedSquare }
        .map { it.toArrow(ChessBoardViewState.Arrow.COLOR_SUGGESTION) }
        .toSet()
}

internal fun Set<Game.HalfMove>.toLastMoveSquares(): Set<OverlaySquare> {
    val lastMove = this.getLastMove() ?: return emptySet()
    return setOf(
        OverlaySquare(lastMove.move.take(2), OverlaySquare.COLOR_LAST_MOVE),
        OverlaySquare(lastMove.move.takeLast(2), OverlaySquare.COLOR_LAST_MOVE),
    )
}

private fun Set<Game.HalfMove>.getLastMove(): Game.HalfMove? =
    sorted().lastOrNull()

internal fun Set<Game.HalfMove>.sorted(): List<Game.HalfMove> = sortedWith { o1, o2 ->
    when {
        o1.number != o2.number -> o1.number - o2.number
        o1.player == Game.Player.White -> -1
        o2.player == Game.Player.White -> 1
        else -> 0
    }
}

internal fun MoveNotation.toLastMoveSquares() = setOf(
    OverlaySquare(this.take(2), OverlaySquare.COLOR_LAST_MOVE),
    OverlaySquare(this.takeLast(2), OverlaySquare.COLOR_LAST_MOVE)
)

internal fun toPlayerState(
    player: Game.Player,
    position: FenNotation,
    topMoves: List<Engine.TopMove>,
    isLoading: Boolean
): PlayerViewState {
    val nextMovePlayer = position.toNextMovePlayer()

    return when {
        nextMovePlayer == Game.Player.None ->
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

internal fun FenNotation.toNextMovePlayer() = when (nextMoveColor.lowercase()) {
    "w" -> Game.Player.White
    "b" -> Game.Player.Black
    else -> Game.Player.None
}

internal fun Engine.TopMove.toArrow(color: Int) = ChessBoardViewState.Arrow(
    start = this.move.take(2),
    end = this.move.takeLast(2),
    color = color,
    strong = (this.source != OPEN_AI_ENGINE_NAME)
)

internal fun Map.Entry<SquareNotation, PieceNotation>.toPiece(selectedSquare: SquareNotation?, pendingMove: MoveNotation?) =
    ChessBoardViewState.Piece(
        drawableName = value.toDrawableName(),
        position = key,
        isElevated = key in setOf(selectedSquare, pendingMove?.takeLast(2))
    )

private fun PieceNotation.toDrawableName(): String {
    val color = if (this.isLowerCase()) "b" else "w"
    val symbol = this.lowercaseChar()
    return "piece_$symbol$color"
}