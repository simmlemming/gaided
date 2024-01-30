package com.gaided

import com.gaided.domain.Engine
import com.gaided.domain.FenNotation
import com.gaided.domain.MoveNotation
import com.gaided.util.toNextMovePlayer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update

internal class Game(private val engine: Engine) {
    private val _position = MutableStateFlow(FenNotation.START_POSITION)
    val position: Flow<FenNotation> = _position.asStateFlow()

    private val _started = MutableStateFlow(false)
    val started = _started.asStateFlow()

    // Cold flow!
    // Each consumer triggers engine.getEvaluation()
    val evaluation = combine(_position, _started) { position, stared ->
        if (stared) mapOf(position to engine.getEvaluation(position)) else emptyMap()
    }

    private val _history = MutableStateFlow<Set<HalfMove>>(emptySet())
    val history: Flow<Set<HalfMove>> = _history.asStateFlow()

    private val topMoves = MutableStateFlow<Map<FenNotation, List<Engine.TopMove>>>(emptyMap())

    internal fun start() {
        _started.value = true
    }

    internal fun getTopMoves(position: FenNotation): Flow<List<Engine.TopMove>> =
        combine(topMoves, started) { cache, _ ->
            cache[position].orEmpty()
        }.onStart {
            emit(topMoves.value[position].orEmpty())
            refreshTopMoves(position)
        }

    private suspend fun refreshTopMoves(position: FenNotation) {
        if (topMoves.value[position] != null) {
            return
        }

        topMoves.update {
            it + (position to engine.getTopMoves(position, 3))
        }
    }

    internal suspend fun move(move: MoveNotation, player: Player = _position.value.toNextMovePlayer()) {
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
    }

    internal suspend fun isMoveIfCorrect(move: MoveNotation) =
        engine.isMoveCorrect(_position.value, move)

    internal data class HalfMove(
        val number: Int,
        val move: MoveNotation,
        val player: Player,
        val positionAfterMove: FenNotation,
    ) {

        override fun toString(): String {
            return "$number. $move (${player::class.simpleName})"
        }

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

        require(lastMove.player != player) {
            "Move of player $player already exists in the history: $lastMove"
        }

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

internal fun Set<Game.HalfMove>.sorted(): List<Game.HalfMove> = sortedWith { o1, o2 ->
    when {
        o1.number != o2.number -> o1.number - o2.number
        o1.player == Game.Player.White -> -1
        o2.player == Game.Player.White -> 1
        else -> 0
    }
}

internal fun Set<Game.HalfMove>.getLastMove(): Game.HalfMove? =
    sorted().lastOrNull()

