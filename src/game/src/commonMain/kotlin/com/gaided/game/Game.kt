package com.gaided.game

import com.gaided.engine.Engine
import com.gaided.game.util.toNextMovePlayer
import com.gaided.model.FenNotation
import com.gaided.model.MoveNotation
import com.gaided.stockfish.board.Board
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.update

class Game(
    private val board: Board,
    private val engines: List<Engine>
) {
    private val _position = MutableStateFlow(FenNotation.START_POSITION)
    val position: Flow<FenNotation> = _position.asStateFlow()

    private val _started = MutableStateFlow(false)
    val started = _started.asStateFlow()

    // Cold flow!
    // Each consumer triggers engine.getEvaluation()
    val evaluation = combine(_position, _started) { position, stared ->
        if (stared) mapOf(position to board.getEvaluation(position)) else emptyMap()
    }

    private val _history = MutableStateFlow<Set<HalfMove>>(emptySet())
    val history: Flow<Set<HalfMove>> = _history.asStateFlow()

    private val topMovesCache = MutableStateFlow<Map<FenNotation, TopMovesProgress>>(emptyMap())

    internal fun start() {
        _started.value = true
    }

    internal fun getTopMoves(position: FenNotation): Flow<TopMovesProgress> =
        topMovesCache.combineTransform(started) { cache, started ->
            val cached = cache[position]
            emit(cached ?: TopMovesProgress(inProgress = true))

            if (!started || cached != null) {
                return@combineTransform
            }

            val allTopMoves = mutableListOf<Engine.TopMove>()
            engines.forEach { engine ->
                val topMoves = engine.getTopMoves(position)
                allTopMoves.addAll(topMoves)
                emit(TopMovesProgress(moves = allTopMoves, inProgress = true))
            }

            emit(TopMovesProgress(moves = allTopMoves))
            topMovesCache.update {
                it + (position to TopMovesProgress(allTopMoves))
            }
        }

    internal suspend fun move(move: MoveNotation, player: Player = _position.value.toNextMovePlayer()) {
        val expectedPlayer = _position.value.toNextMovePlayer()
        check(expectedPlayer == player) {
            "Expected player to move $expectedPlayer, was $player"
        }

        board.move(_position.value, move)
        val position = board.getPosition()
        _position.value = position

        _history.update {
            it.add(player, move, position.fenString)
        }
    }

    internal suspend fun isMoveIfCorrect(move: MoveNotation) =
        board.isMoveCorrect(_position.value, move)

    internal data class TopMovesProgress(
        val moves: List<Engine.TopMove> = emptyList(),
        val inProgress: Boolean = false
    )

    data class HalfMove(
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

