package com.gaided

import com.gaided.domain.Board
import com.gaided.domain.Engine
import com.gaided.domain.FEN_START_POSITION
import com.gaided.domain.MoveNotation
import com.gaided.model.*
import com.gaided.model.playerToMove
import com.gaided.model.setMove
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlin.random.Random

internal class Game(
    private val engine: Engine,
    val board: Board
) {

    private val _state = MutableStateFlow(State.INIT)
    val state = _state.asStateFlow()

    private val rnd = Random(System.currentTimeMillis())

    internal suspend fun start() {
        engine.setFenPosition(FEN_START_POSITION)
        _state.value = _state.value.copy(currentMove = PartialHistoryRecord())

        val topMoves = engine.getTopMoves(3)
        board.setTopMoves(topMoves)
        _state.setTopMoves(Player.White, topMoves)
    }

    internal suspend fun move(player: Player, move: MoveNotation) {
        val currentState = state.value
        require(currentState.playerToMove == player) {
            "Did not expect $player to move. Current state = $currentState"
        }

        // Move
        _state.setMove(player, move)

        // Update history and current state if move is complete (white and black moved)
        if (player == Player.Black) {
            _state.value = state.value.copy(
                history = state.value.history + state.value.currentMove!!.toHistoryRecord(),
                currentMove = PartialHistoryRecord()
            )
        }

        // Update board with new position
        board.setTopMoves(emptyList())
        engine.makeMoveFromCurrentPosition(move.take(2), move.takeLast(2))
        val fenPosition = engine.getFenPosition()
        board.setPosition(fenPosition)

        // Update top moves
        val topMoves = engine.getTopMoves(3)
        board.setTopMoves(topMoves)
        val otherPlayer = if (player == Player.White) Player.Black else Player.White
        _state.setTopMoves(otherPlayer, topMoves)

        // Make next move
        if (otherPlayer == Player.Black) {
            delay(rnd.nextLong(500, 2000))
            topMoves
                .randomOrNull(rnd)
                ?.let { move(otherPlayer, it.move) }
        }
    }

    internal data class State(
        val history: List<HistoryRecord>,
        val currentMove: PartialHistoryRecord? = null
    ) {
        companion object {
            val INIT = State(history = listOf(), currentMove = null)
        }
    }

    sealed class Player {
        object White : Player()
        object Black : Player()
        object None : Player()
    }

    internal data class Stats(val w: Int, val d: Int, val b: Int)

    internal data class PartialHistoryRecord(
        val whiteMove: MoveNotation? = null,
        val statsAfterWhiteMove: Stats? = null,
        val whiteTopMoves: List<Engine.TopMove>? = null,
        val blackMove: MoveNotation? = null,
        val statsAfterBlackMove: Stats? = null,
        val blackTopMoves: List<Engine.TopMove>? = null
    )

    internal data class HistoryRecord(
        val whiteMove: MoveNotation,
        val statsAfterWhiteMove: Stats,
        val whiteTopMoves: List<Engine.TopMove>,
        val blackMove: MoveNotation,
        val statsAfterBlackMove: Stats,
        val blackTopMoves: List<Engine.TopMove>
    )
}