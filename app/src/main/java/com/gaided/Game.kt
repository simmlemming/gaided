package com.gaided

import com.gaided.domain.Board
import com.gaided.domain.Engine
import com.gaided.domain.FEN_START_POSITION
import com.gaided.domain.MoveNotation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class Game(
    private val engine: Engine,
    val board: Board
) {

    private val _state = MutableStateFlow<State>(State.Initialized)
    val state = _state.asStateFlow()

    internal suspend fun start() {
        engine.setFenPosition(FEN_START_POSITION)
        _state.value = State.WaitingForMove(Player.White, setOf(), false)
    }

    internal suspend fun move(player: Player, move: MoveNotation) {
        val currentState = state.value
        require(currentState is State.WaitingForMove)
        require(currentState.player == player) { "Expected ${currentState.player} to move." }

        val otherPlayer = if (player == Player.White) Player.Black else Player.White

        _state.value = State.WaitingForMove(otherPlayer, emptySet(), false)

        board.setTopMoves(emptyList())
        engine.makeMoveFromCurrentPosition(move.take(2), move.takeLast(2))
        val fenPosition = engine.getFenPosition()
        board.setPosition(fenPosition)

        _state.value = State.WaitingForMove(otherPlayer, emptySet(), true)
        val topMoves = engine.getTopMoves(3)
        board.setTopMoves(topMoves)

        _state.value = State.WaitingForMove(otherPlayer, topMoves.toSet(), false)
    }

    internal suspend fun selectTopMove(player: Player, move: MoveNotation) {
        val currentState = state.value
        require(currentState is State.WaitingForMove)

        val moveToMake = currentState.topMoves.find { it.move == move }
        requireNotNull(moveToMake)

        move(player, moveToMake.move)
    }

    sealed class Player {
        object White : Player()
        object Black : Player()
    }

    sealed class State {
        object Initialized : State()

        data class WaitingForMove(
            val player: Player,
            val topMoves: Set<Engine.TopMove>,
            val waitingForTopMoves: Boolean
        ) : State()
    }
}