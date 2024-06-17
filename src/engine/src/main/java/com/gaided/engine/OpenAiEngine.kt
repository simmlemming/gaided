package com.gaided.engine

import com.gaided.engine.Engine.TopMove
import com.gaided.engine.api.OPEN_AI_MODEL
import com.gaided.engine.api.OpenAiEngineApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

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
            matchers.firstNotNullOfOrNull { matcher -> matcher(move) }
        }

        val parsedMoves = topMoves.map { it.move }
        logger.i("$name: '$response' -> $parsedMoves")

        topMoves
    }

    private val matchers: List<((String) -> TopMove?)> = listOf(
        ::matcher1,
        ::matcher2,
        ::matcher3,
        ::matcher4
    )

    private fun matcher4(move: String): TopMove? {
        val regex = "([a-z][1-8]) to ([a-z][1-8])".toRegex()
        val result = regex.find(move) ?: return null
        val groups = result.groupValues

        if (groups.size != 3) {
            return null
        }

        return TopMove(name, "${groups[1]}${groups[2]}")
    }

    private fun matcher3(move: String): TopMove? {
        val regex = "([a-z][1-8])-([a-z][1-8])".toRegex()
        val result = regex.find(move) ?: return null
        val groups = result.groupValues

        if (groups.size != 3) {
            return null
        }

        return TopMove(name, "${groups[1]}${groups[2]}")
    }

    private fun matcher2(move: String): TopMove? {
        val regex = "([a-z][1-8])x([a-z][1-8])".toRegex()
        val result = regex.find(move) ?: return null
        val groups = result.groupValues

        if (groups.size != 3) {
            return null
        }

        return TopMove(name, "${groups[1]}${groups[2]}")
    }

    private fun matcher1(move: String): TopMove? {
        val regex = "([a-z][1-8])([a-z][1-8])".toRegex()
        val result = regex.find(move) ?: return null
        val groups = result.groupValues

        if (groups.size != 3) {
            return null
        }

        return TopMove(name, "${groups[1]}${groups[2]}")
    }
}
