package com.gaided

import com.gaided.domain.Engine
import com.gaided.domain.FenNotation
import com.gaided.domain.MoveNotation
import com.gaided.util.toNextMovePlayer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

internal class Game(
    private val engine: Engine
) {

    private val _position = MutableStateFlow(FenNotation.START_POSITION)
    val position: Flow<FenNotation> = _position.asStateFlow()

    private val _topMoves = MutableStateFlow<Map<FenNotation, List<Engine.TopMove>>>(emptyMap())
    val topMoves: Flow<Map<FenNotation, List<Engine.TopMove>>> = _topMoves.asStateFlow()

    private val _history = MutableStateFlow<Set<HalfMove>>(emptySet())
    val history: Flow<Set<HalfMove>> = _history.asStateFlow()

    private val _evaluation = MutableStateFlow<Map<FenNotation, Engine.Evaluation>>(emptyMap())
    val evaluation = _evaluation.asStateFlow()

    private val rnd = Random(System.currentTimeMillis())

    internal suspend fun start() {
        requestEvaluation(_position.value)
        requestTopMoves(_position.value)
    }

    internal suspend fun move(player: Player, move: MoveNotation) {
        val expectedPlayer = _position.value.toNextMovePlayer()
        check(expectedPlayer == player) {
            "Expected player to move $expectedPlayer, was $player"
        }

        engine.move(_position.value, move)
        val fenPosition = engine.getFenPosition()
        _position.value = FenNotation.fromFenString(fenPosition)

        _history.update {
            it.add(player, move, fenPosition)
        }

        requestEvaluation(_position.value)
        requestTopMoves(_position.value)
    }

    private suspend fun requestTopMoves(position: FenNotation) {
        val topMoves = engine.getTopMoves(position, 3)
        _topMoves.update {
            it + (position to topMoves)
        }
    }

    private suspend fun requestEvaluation(position: FenNotation) {
        val evaluation = engine.getEvaluation(position)
        _evaluation.update {
            it + (position to evaluation)
        }
    }

    internal data class HalfMove(
        val number: Int,
        val move: MoveNotation,
        val player: Player,
        val positionAfterMove: FenNotation,
    ) {
        override fun equals(other: Any?): Boolean {
            if (other !is HalfMove) return false
            return (this.number == other.number && this.player == other.player)
        }

        override fun hashCode(): Int {
            return number * 31 + player.hashCode()
        }
    }

    private fun Set<HalfMove>.add(player: Player, move: MoveNotation, fenPosition: String): Set<HalfMove> {
        val lastMove = this.getLastMove()

        if (lastMove == null) {
            require(this.isEmpty())
            require(player == Player.White)
            return this + HalfMove(1, move, player, FenNotation.fromFenString(fenPosition))
        }

        require(lastMove.player != player)

        val newMoveNumber = if (player == Player.White) {
            lastMove.number + 1
        } else {
            lastMove.number
        }

        return this + HalfMove(newMoveNumber, move, player, FenNotation.fromFenString(fenPosition))
    }

    sealed class Player {
        object White : Player()
        object Black : Player()
        object None : Player()
    }
}

internal fun Set<Game.HalfMove>.getLastMove(): Game.HalfMove? {
    return sortedWith { o1, o2 ->
        when {
            o1.number != o2.number -> o1.number - o2.number
            o1.player == Game.Player.White -> 1
            o2.player == Game.Player.White -> -1
            else -> 0
        }
    }.lastOrNull()
}
