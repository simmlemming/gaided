package com.gaided.engine.api

import com.gaided.engine.DefaultLogger
import com.gaided.engine.Logger
import com.google.gson.Gson
import java.net.URL

internal const val OPEN_AI_MODEL = "gpt-4o"

internal class OpenAiEngineApi(
    private val apiKey: String,
    logger: Logger = DefaultLogger
) : HttpApi(logger = logger) {

    private val gson = Gson()

    fun getTopMoves(position: String, numberOfMoves: Int): String {
        return post {
            url = URL("https://api.openai.com/v1/chat/completions")
            headers["Authorization"] = "Bearer $apiKey"
            headers["Content-Type"] = "application/json"
            parse = { gson.parseOpenAiResponse(it) }
            body = """
                {
                    "model": "$OPEN_AI_MODEL",
                    "messages": [
                        {
                            "role": "system",
                            "content": "You will be provided with chess FEN position, and your task is to find $numberOfMoves best moves. You reply with comma separated string of moves. You use fully expanded algebraic notation."
                          },
                          {
                            "role": "user",
                            "content": "$position"
                          }
                        ]
                }
            """.trimIndent()
        }
    }
}
// "content": "FEN position: '$position'. What are top $numberOfMoves moves?"

private fun Gson.parseOpenAiResponse(response: String) =
    fromJson(response, OpenAiResponse::class.java).choices.firstOrNull()?.message?.content ?: "-"

public data class OpenAiResponse(
    val choices: List<Choice>
) {
    public data class Choice(
        val message: Message
    )

    public data class Message(
        val content: String
    )
}

//public fun main() {
//    val topMoves = OpenAiEngineApi().getTopMoves(
//        position = "rnbqkb1r/1pp1pppp/p4n2/3P4/3P4/2N5/PP2PPPP/R1BQKBNR b KQkq - 0 4",
//        numberOfMoves = 3
//    )
//    println(topMoves)
//}
