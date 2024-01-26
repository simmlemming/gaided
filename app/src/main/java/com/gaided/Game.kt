package com.gaided

import com.gaided.domain.Engine
import com.gaided.domain.FenNotation
import com.gaided.domain.MoveNotation
import com.gaided.util.toNextMovePlayer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

internal class Game(private val engine: Engine) {
    private val _position = MutableStateFlow(FenNotation.START_POSITION)
    val position: Flow<FenNotation> = _position.asStateFlow()

    // Cold flow!
    // Each consumer triggers engine.getTopMoves()
    val topMoves = _position.map {
        mapOf(it to engine.getTopMoves(it, 3))
    }

    // Cold flow!
    // Each consumer triggers engine.getEvaluation()
    val evaluation = _position.map {
        mapOf(it to engine.getEvaluation(it))
    }

    private val _history = MutableStateFlow<Set<HalfMove>>(emptySet())
    val history: Flow<Set<HalfMove>> = _history.asStateFlow()

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

