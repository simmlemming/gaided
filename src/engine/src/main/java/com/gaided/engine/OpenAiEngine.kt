package com.gaided.engine

import com.gaided.engine.Engine.TopMove
import com.gaided.engine.api.OPEN_AI_MODEL
import com.gaided.engine.api.OpenAiEngineApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

@Suppress("UNUSED_PARAMETER")
public class OpenAiEngine(
    private val api: OpenAiEngineApi,
    private val ioContext: CoroutineContext = Dispatchers.IO,
    private val logger: Logger = DefaultLogger
) : Engine {

    public companion object {
        public const val NAME: String = "OpenAI $OPEN_AI_MODEL"
    }

    override val name: String = NAME

    override suspend fun getTopMoves(
        position: FenNotation, numberOfMoves: Int
    ): List<TopMove> = withContext(ioContext) {
        val response = api.getTopMoves(position.fenString, numberOfMoves)
        val movesAsText = response.split(",").take(numberOfMoves)

        val topMoves = movesAsText
            .map { it.trim() }
            .mapNotNull { move ->
                val result1 = matchers2.firstNotNullOfOrNull { (regex, matcher) ->
                    val groups = regex.matchEntire(move)?.groupValues.orEmpty()
                    val topMove = matcher(position, move, groups)
                    topMove
                }
                result1
            }

        val parsedMoves = topMoves.map { it.move }
        logger.i("$name: '$response' -> $parsedMoves")

        topMoves
    }

    private val matchers2: Map<Regex, (FenNotation, MoveNotation, List<String>) -> TopMove?> = mapOf(
        "[A-Z]?([a-z][1-8])x([a-z][1-8])".toRegex() to ::fullNotation,
        "[A-Z]?([a-z][1-8])([a-z][1-8])".toRegex() to ::fullNotation,
        "[A-Z]?([a-z][1-8])-([a-z][1-8])".toRegex() to ::fullNotation,
        "([a-z][1-8]) to ([a-z][1-8])".toRegex() to ::fullNotation,
        "([a-z][1-8])".toRegex() to ::shortNotationPawnMoves,
        "([a-z])x([a-z][1-8])".toRegex() to ::shortNotationPawnTakes,
        "R([a-z][1-8])".toRegex() to ::shortNotationRookMoves,
    )

    private fun shortNotationRookMoves(position: FenNotation, move: MoveNotation, groups: List<String>): TopMove? {
        if (groups.size != 2) {
            return null
        }

        val to = groups[1]
        val expectedPiece = if (position.nextMoveColor == "w") 'R' else 'r'

        val from = findFromSquare(
            position, to, expectedPiece,
            listOf(
                { file, row -> "$file${row - 1}" },
                { file, row -> "$file${row - 2}" },
                { file, row -> "$file${row - 3}" },
                { file, row -> "$file${row - 4}" },
                { file, row -> "$file${row - 5}" },
                { file, row -> "$file${row - 6}" },
                { file, row -> "$file${row - 7}" },
                { file, row -> "$file${row + 1}" },
                { file, row -> "$file${row + 2}" },
                { file, row -> "$file${row + 3}" },
                { file, row -> "$file${row + 4}" },
                { file, row -> "$file${row + 5}" },
                { file, row -> "$file${row + 6}" },
                { file, row -> "$file${row + 7}" },
                { file, row -> "${file - 1}$row" },
                { file, row -> "${file - 2}$row" },
                { file, row -> "${file - 3}$row" },
                { file, row -> "${file - 4}$row" },
                { file, row -> "${file - 5}$row" },
                { file, row -> "${file - 6}$row" },
                { file, row -> "${file - 7}$row" },
                { file, row -> "${file - 8}$row" },
                { file, row -> "${file + 1}$row" },
                { file, row -> "${file + 2}$row" },
                { file, row -> "${file + 3}$row" },
                { file, row -> "${file + 4}$row" },
                { file, row -> "${file + 5}$row" },
                { file, row -> "${file + 6}$row" },
                { file, row -> "${file + 7}$row" },
                { file, row -> "${file + 8}$row" },
            )
        )

        return from?.let { TopMove(name, "$it$to") }
    }

    @Suppress("MoveLambdaOutsideParentheses")
    private fun shortNotationPawnTakes(position: FenNotation, move: MoveNotation, groups: List<String>): TopMove? {
        if (groups.size != 3) {
            return null
        }

        val fromFile = groups[1]
        val to = groups[2]
        var from: String? = null

        if (position.nextMoveColor == "w") {
            from = findFromSquare(
                position, to, 'P',
                listOf({ _, row -> "$fromFile${row - 1}" })
            )
        }

        if (position.nextMoveColor == "b") {
            from = findFromSquare(
                position, to, 'p',
                listOf({ _, row -> "$fromFile${row + 1}" })
            )
        }

        return from?.let { TopMove(name, "$it$to") }
    }

    private fun shortNotationPawnMoves(position: FenNotation, move: MoveNotation, groups: List<String>): TopMove? {
        if (groups.size != 2) {
            return null
        }

        val to = groups[1]
        var from: String? = null

        if (position.nextMoveColor == "w") {
            from = findFromSquare(
                position, to, 'P',
                listOf({ file, row -> "$file${row - 1}" }, { file, row -> "$file${row - 2}" })
            )
        }

        if (position.nextMoveColor == "b") {
            from = findFromSquare(
                position, to, 'p',
                listOf({ file, row -> "$file${row + 1}" }, { file, row -> "$file${row + 2}" })
            )
        }

        return from?.let { TopMove(name, "$it$to") }
    }

    private fun findFromSquare(
        position: FenNotation,
        toSquare: SquareNotation,
        expectedPiece: PieceNotation,
        candidates: List<(Char, Int) -> SquareNotation>
    ): SquareNotation? {
        val toRow = toSquare.take(1)[0]
        val toFile = toSquare.takeLast(1).toInt()

        val map = candidates
            .map {
                val candidateSquare = it.invoke(toRow, toFile)
                val piece = try {
                    position.pieceAt(candidateSquare)
                } catch (e: Exception) {
                    null
                }
                candidateSquare to piece
            }
        val entry = map
            .firstOrNull { it.second == expectedPiece }

        return entry?.first
    }

    private fun fullNotation(position: FenNotation, move: MoveNotation, groups: List<String>): TopMove? {
        if (groups.size != 3) {
            return null
        }

        return TopMove(name, "${groups[1]}${groups[2]}")
    }
}
