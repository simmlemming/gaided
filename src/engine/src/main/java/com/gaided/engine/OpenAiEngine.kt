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

        val topMoves = movesAsText.mapNotNull { move ->
            matchers.firstNotNullOfOrNull { matcher -> matcher(position, move.trim()) }
        }

        val parsedMoves = topMoves.map { it.move }
        logger.i("$name: '$response' -> $parsedMoves")

        topMoves
    }

    private val matchers: List<((FenNotation, String) -> TopMove?)> = listOf(
        ::matcher1,
        ::matcher2,
        ::matcher3,
        ::matcher4,
        ::matcher5
    )

    private fun matcher5(position: FenNotation, move: String): TopMove? {
        val regex = "([a-z][1-8])".toRegex()
        val result = regex.matchEntire(move) ?: return null
        val groups = result.groupValues

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
        candidates: List<(String, Int) -> SquareNotation>
    ): SquareNotation? {
        val toRow = toSquare.take(1)
        val toFile = toSquare.takeLast(1).toInt()

        val entry = candidates
            .map {
                val candidateSquare = it.invoke(toRow, toFile)
                val piece = position.pieceAt(candidateSquare)
                candidateSquare to piece
            }
            .firstOrNull { it.second == expectedPiece }

        return entry?.first
    }

    private fun matcher4(position: FenNotation, move: String): TopMove? {
        val regex = "([a-z][1-8]) to ([a-z][1-8])".toRegex()
        val result = regex.find(move) ?: return null
        val groups = result.groupValues

        if (groups.size != 3) {
            return null
        }

        return TopMove(name, "${groups[1]}${groups[2]}")
    }

    private fun matcher3(position: FenNotation, move: String): TopMove? {
        val regex = "([a-z][1-8])-([a-z][1-8])".toRegex()
        val result = regex.find(move) ?: return null
        val groups = result.groupValues

        if (groups.size != 3) {
            return null
        }

        return TopMove(name, "${groups[1]}${groups[2]}")
    }

    private fun matcher2(position: FenNotation, move: String): TopMove? {
        val regex = "([a-z][1-8])x([a-z][1-8])".toRegex()
        val result = regex.find(move) ?: return null
        val groups = result.groupValues

        if (groups.size != 3) {
            return null
        }

        return TopMove(name, "${groups[1]}${groups[2]}")
    }

    private fun matcher1(position: FenNotation, move: String): TopMove? {
        val regex = "([a-z][1-8])([a-z][1-8])".toRegex()
        val result = regex.find(move) ?: return null
        val groups = result.groupValues

        if (groups.size != 3) {
            return null
        }

        return TopMove(name, "${groups[1]}${groups[2]}")
    }
}
