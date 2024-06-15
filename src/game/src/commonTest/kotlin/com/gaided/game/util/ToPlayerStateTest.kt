package com.gaided.game.util

import com.gaided.engine.Engine
import com.gaided.engine.FenNotation
import com.gaided.game.Game
import com.gaided.game.ui.model.PlayerViewState
import org.junit.Assert.assertEquals
import org.junit.Test

internal class ToPlayerStateTest {

    @Test
    fun `waiting for top moves for other player`() {
        assertEquals(
            PlayerViewState(
                progressVisible = false,
                movesStats = emptyList()
            ),
            toPlayerState(
                player = Game.Player.Black,
                position = FenNotation.START_POSITION,
                topMoves = emptyList()
            )
        )
    }

    @Test
    fun `waiting for top moves for self`() {
        assertEquals(
            PlayerViewState(
                progressVisible = true,
                movesStats = emptyList()
            ),
            toPlayerState(
                player = Game.Player.White,
                position = FenNotation.START_POSITION,
                topMoves = emptyList()
            )
        )
    }

    @Test
    fun `have top moves`() {
        assertEquals(
            PlayerViewState(
                progressVisible = false,
                movesStats = emptyList()
            ),
            toPlayerState(
                player = Game.Player.White,
                position = FenNotation.START_POSITION,
                topMoves = listOf(Engine.TopMove("any", "a2a4", 0))
            )
        )
    }
}