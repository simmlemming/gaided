package com.gaided

import com.gaided.domain.Board
import com.gaided.domain.Engine
import com.gaided.domain.FEN_START_POSITION
import com.gaided.domain.SquareNotation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class Game(
    private val engine: Engine,
    val board: Board
) {

    private val _state = MutableStateFlow(State.Initialized)
    val state = _state.asStateFlow()

    internal suspend fun start() {
        engine.setFenPosition(FEN_START_POSITION)
    }

    internal suspend fun move(player: Player, from: SquareNotation, to: SquareNotation) {
        engine.makeMoveFromCurrentPosition(from, to)


        val fenPosition = engine.getFenPosition()
        board.setPosition(fenPosition)

        val topMoves = engine.getTopMoves(3)
        board.setTopMoves(topMoves)


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