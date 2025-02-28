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

    private val _players = MutableStateFlow<Pair<Player, Player>?>(null)

    private val _state = MutableStateFlow<State>(State.Created(FenNotation.START_POSITION))
//    val state = _state.asStateFlow()

    val state: Flow<State> = combine(_state, evaluation, _players) { state, eval, players ->
        TODO()
    }

    private suspend fun loop(
        state: State,
        evaluations: Map<FenNotation, Board.Evaluation>,
        playerWhite: Player,
        playerBlack: Player,
    ): State {
        if (state is State.Finished) {
            return state
        }

        if (state is State.Created) {
            return State.WaitingForMove(
                position = state.position,
                nextMovePlayerColor = Player.Color.White,
                evaluation = evaluations[state.position]
            )
        }

        state as State.WaitingForMove

        val position = state.position
        val move: MoveNotation? = getNextCorrectMove(position, playerWhite, playerBlack)

        if (move == null) {
            Logger.e("Move is null", null) // TODO: What to do?
            return State.Finished(winner = null, position = position)
        }

        board.move(position, move)
        val newPosition = board.getPosition()

        return State.WaitingForMove(
            nextMovePlayerColor = newPosition.toNextMovePlayer(),
            position = newPosition,
            evaluation = null
        )
    }

    private suspend fun getNextCorrectMove(
        position: FenNotation,
        playerWhite: Player, playerBlack: Player,
        attempt: Int = 0
    ): MoveNotation? {
        if (attempt > 3) {
            return null
        }

        val player = when (position.toNextMovePlayer()) {
            Player.Color.White -> playerWhite
            Player.Color.Black -> playerBlack
            else -> null
        } ?: return null

        val move = player.getMove(position) ?: return null

        if (!board.isMoveCorrect(position, move)) {
            return getNextCorrectMove(position, playerWhite, playerBlack, attempt + 1)
        }

        return move
    }

    fun start() {
        _started.value = true
    }

    suspend fun play(
        playerWhite: Player,
        playerBlack: Player,
    ) {
        _started.value = true
        _players.value = Pair(playerWhite, playerBlack)

//        var state: State = State.Created()
//
//        while (state !is State.Finished) {
//            state = loop(
//                state = state,
//                evaluations = emptyMap(),
//                playerWhite = playerWhite,
//                playerBlack = playerBlack,
//            )
//        }

//        // Starts the loop, that ends when value == State.Finished
//        val position = FenNotation.START_POSITION
//        _state.value = State.WaitingForMove(
//            nextMovePlayerColor = position.toNextMovePlayer(),
//            position = position,
//            evaluation = null
//        )
//
//        _state.collect {
//            _state.update {
//                loop(it, emptyMap(), playerWhite, playerBlack)
//            }
//        }
    }

    suspend fun start(
        playerWhite: Player,
        playerBlack: Player,
    ) {
        _started.value = true
        _players.value = Pair(playerWhite, playerBlack)

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

    sealed class State {
        data class WaitingForMove(
            val nextMovePlayerColor: Player.Color,
            val position: FenNotation,
            val evaluation: Board.Evaluation? = null,
        ) : State()

        class Finished(
            val winner: Player.Color?,
            val position: FenNotation,
        ) : State()

        data class Created(
            val position: FenNotation = FenNotation.START_POSITION,
        ) : State()
    }

    fun getTopMoves(position: FenNotation): Flow<TopMovesProgress> =
        topMovesCache.combineTransform(_started) { cache, started ->
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

