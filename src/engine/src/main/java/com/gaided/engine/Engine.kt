package com.gaided.engine

public interface Engine {
    public val name: String
    public val recommendedNumberOfMoves: Int

    public suspend fun getTopMoves(position: FenNotation, numberOfMoves: Int = recommendedNumberOfMoves): List<TopMove>

    public data class TopMove(
        public val source: String,
        public val move: String,
        public val centipawn: Int? = null
    )
}