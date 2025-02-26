package com.gaided.fortress.app.android

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Person
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
        createPlayer = { playerType, color ->
            when (playerType) {
                is FortressViewModel.PlayerType.Human ->
                    object : ChessGame.Player {
                        override val color = color
                        override val name = playerType.name
                        override suspend fun getMove(position: FenNotation): MoveNotation? {
                            delay(Long.MAX_VALUE)
                            throw RuntimeException()
                        }
                    }

                is FortressViewModel.PlayerType.Stockfish ->
                    object : ChessGame.Player {
                        override val color = color
                        override val name = "Stockfish 15"
                        override suspend fun getMove(position: FenNotation): MoveNotation? {
                            delay(Long.MAX_VALUE)
                            throw RuntimeException()
                        }
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

        viewModel.startFortressGame(
            playerTypeWhite = FortressViewModel.PlayerType.Human("Person name"),
            playerTypeBlack = FortressViewModel.PlayerType.Stockfish,
        )

        assertEquals(
            FortressPlayerViewState(
                progressVisible = true,
                name = "Person name",
                icon = Icons.Default.Person,
            ),
            viewModel.playerWhite.value,
        )

        assertEquals(
            FortressPlayerViewState(
                progressVisible = false,
                name = "Stockfish 15",
                icon = Icons.Default.Computer,
            ),
            viewModel.playerBlack.value,
        )
    }

    // TODO: Add test to check player states after white move.
}