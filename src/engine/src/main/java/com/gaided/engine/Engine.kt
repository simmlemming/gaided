package com.gaided.engine

import com.gaided.model.FenNotation
import com.gaided.model.MoveNotation

public interface Engine {
    public val name: String
    public val recommendedNumberOfMoves: Int

    public suspend fun getTopMoves(position: FenNotation, numberOfMoves: Int = recommendedNumberOfMoves): List<TopMove>

    public data class TopMove(
        public val source: String,
        public val move: MoveNotation,
        public val centipawn: Int? = null
    )
}