package com.gaided

import com.gaided.domain.Engine
import com.gaided.domain.FenNotation
import com.gaided.domain.MoveNotation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val rnd = Random(System.currentTimeMillis())

    internal fun start() {
        _position.value = FenNotation.START_POSITION
    }

    internal suspend fun move(player: Player, move: MoveNotation) {

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

    private fun Set<HalfMove>.add(halfMove: HalfMove): Set<HalfMove> {
        TODO()
    }

    private fun Set<HalfMove>.get(number: Int, player: Player): HalfMove? {
        TODO()
    }

    private fun Set<HalfMove>.getLast(): HalfMove? {
        TODO()
    }

    sealed class Player {
        object White : Player()
        object Black : Player()
        object None : Player()
    }

    internal data class Stats(val w: Int, val d: Int, val b: Int)
}