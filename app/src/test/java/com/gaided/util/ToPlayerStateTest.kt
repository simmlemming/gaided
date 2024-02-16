package com.gaided.util

import com.gaided.Game
import com.gaided.domain.Engine
import com.gaided.domain.FenNotation
import com.gaided.view.player.PlayerView
import org.junit.Assert.assertEquals
import org.junit.Test

internal class ToPlayerStateTest {

    @Test
    fun `waiting for top moves for other player`() {
        assertEquals(
            PlayerView.State(
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
            PlayerView.State(
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
            PlayerView.State(
                progressVisible = false,
                movesStats = emptyList()
            ),
            toPlayerState(
                player = Game.Player.White,
                position = FenNotation.START_POSITION,
                topMoves = listOf(Engine.TopMove("a2a4", 0))
            )
        )
    }
}