package com.gaided.engine

import com.gaided.engine.RemoteBoard.TopMove

public interface Engine {
    public val name: String
    public suspend fun getTopMoves(position: FenNotation, numberOfMoves: Int): List<TopMove>
}