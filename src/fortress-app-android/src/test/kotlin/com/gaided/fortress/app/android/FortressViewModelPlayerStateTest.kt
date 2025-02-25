package com.gaided.fortress.app.android

import com.gaided.chessgame.ChessGame
import com.gaided.fortress.app.android.ui.FortressPlayerViewState
import com.gaided.model.FenNotation
import com.gaided.model.MoveNotation
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class FortressViewModelPlayerStateTest : FortressViewModelTestCase() {

    @BeforeTest
    fun setUpFortressTest() {
        createPlayer = { _, color ->
            object : ChessGame.Player {
                override val color = color
                override suspend fun getMove(position: FenNotation): MoveNotation? {
                    delay(Long.MAX_VALUE)
                    throw RuntimeException()
                }
            }
        }
    }

    @Test
    fun `player state at start`() = runTest {
        // GIVEN
        coEvery { board.setPosition(any()) } just Runs
        coEvery { board.getPosition() } returns POSITION_AT_START

        val viewModel = createViewModelAndCollectState()

        viewModel.startFortressGame(FortressViewModel.Player.STOCKFISH, FortressViewModel.Player.STOCKFISH)

        assertEquals(
            FortressPlayerViewState(progressVisible = true),
            viewModel.playerWhite.value,
        )

        assertEquals(
            FortressPlayerViewState(progressVisible = false),
            viewModel.playerBlack.value,
        )
    }

    // TODO: Add test to check player states after white move.
}