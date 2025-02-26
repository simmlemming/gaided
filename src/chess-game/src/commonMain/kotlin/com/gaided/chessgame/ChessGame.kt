package com.gaided.chessgame

import com.gaided.board.stockfish.Board
import com.gaided.engine.Engine
import com.gaided.logger.Logger
import com.gaided.model.FenNotation
import com.gaided.model.MoveNotation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.update

class ChessGame(
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

    fun start() {
        _started.value = true
    }

    // TODO: Add start position, for example "5R2/8/K7/8/8/8/7k/6q1 w - - 0 77"
    suspend fun start(
        playerWhite: Player,
        playerBlack: Player,
    ) {
        _started.value = true

        position.collect { position ->
            val player = when (position.toNextMovePlayer()) {
                Player.Color.White -> playerWhite
                Player.Color.Black -> playerBlack
                else -> null
            } ?: return@collect

            println("pos update, player = ${player.color::class.simpleName}")
            suspend fun getCorrectMove(player: Player): MoveNotation? {
                var move = player.getMove(position)
                println("   $move from ${player.color::class.simpleName}")
                while (move != null && !isMoveCorrect(move)) {
                    move = player.getMove(position)
                }

                return move
            }

            val move = getCorrectMove(player)
            if (move != null) {
                move(move, player.color)
            } else {
                Logger.e("move is null", null)
            }
        }
    }

//    val state = flow<State> {
//        var state: State = State.WaitingForMove(TODO())
//
//        while (state !is State.Ended) {
//            val move = state.player.getMove(state.position)
//            move(move!!)
//            state = State.WaitingForMove(TODO())
//        }
//
//        board.getPosition()
//
//    }

//    sealed class State(val position: FenNotation) {
//        class WaitingForMove(val player: Player, position: FenNotation) : State(position)
//        class Ended(val result: String, position: FenNotation) : State(position)
//    }

//    private suspend fun loop(): Result {
//        position.
//    }

//    sealed class Result {
//        data class Win(val player: Player) : Result()
//        data object Draw : Result()
//        data class Error(val message: String) : Result()
//    }

    fun getTopMoves(position: FenNotation): Flow<TopMovesProgress> =
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

    suspend fun move(move: MoveNotation, player: Player.Color = _position.value.toNextMovePlayer()) {
        println("game.move($move)")
        val expectedPlayer = _position.value.toNextMovePlayer()
        check(expectedPlayer == player) {
            "Expected player to move $expectedPlayer, was $player"
        }

        board.move(_position.value, move)
        val position = board.getPosition()
        println("game.position <- $position")
        _position.value = position

        _history.update {
            it.add(player, move, position.fenString)
        }
    }

    private fun FenNotation.toNextMovePlayer() = when (nextMoveColor.lowercase()) {
        "w" -> Player.Color.White
        "b" -> Player.Color.Black
        else -> Player.Color.None
    }

    suspend fun isMoveCorrect(move: MoveNotation) =
        board.isMoveCorrect(_position.value, move)

    data class TopMovesProgress(
        val moves: List<Engine.TopMove> = emptyList(),
        val inProgress: Boolean = false
    )

    data class HalfMove(
        val number: Int,
        val move: MoveNotation,
        val player: Player.Color,
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

    private fun Set<HalfMove>.add(player: Player.Color, move: MoveNotation, fenPosition: String): Set<HalfMove> {
        val lastMove = this.getLastMove()

        if (lastMove == null) {
            require(this.isEmpty())
            require(player == Player.Color.White)
            return this + HalfMove(1, move, player, FenNotation.fromFenString(fenPosition))
        }

        require(lastMove.player != player) {
            "Move of player $player already exists in the history: $lastMove"
        }

        val newMoveNumber = if (player == Player.Color.White) {
            lastMove.number + 1
        } else {
            lastMove.number
        }

        return this + HalfMove(newMoveNumber, move, player, FenNotation.fromFenString(fenPosition))
    }

    interface Player {
        val color: Color
        val name: String
        suspend fun getMove(position: FenNotation): MoveNotation?

        sealed class Color {
            data object White : Color()
            data object Black : Color()
            data object None : Color()
        }
    }
}

internal fun Set<ChessGame.HalfMove>.sorted(): List<ChessGame.HalfMove> = sortedWith { o1, o2 ->
    when {
        o1.number != o2.number -> o1.number - o2.number
        o1.player == ChessGame.Player.Color.White -> -1
        o2.player == ChessGame.Player.Color.White -> 1
        else -> 0
    }
}

internal fun Set<ChessGame.HalfMove>.getLastMove(): ChessGame.HalfMove? =
    sorted().lastOrNull()

