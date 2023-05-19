@file:Suppress("SpellCheckingInspection")

package gaided

import com.gaided.domain.FenNotation
import org.junit.Assert.assertEquals
import org.junit.Test

internal class FenNotationTest {

    @Test
    fun move() {
        val cases = listOf(
            FenMoveCase(
                from = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                move = "d2d4",
                expected = "rnbqkbnr/pppppppp/8/8/3P4/8/PPP1PPPP/RNBQKBNR b"
            ),
            FenMoveCase(
                from = "rnbqkbnr/ppp1pppp/8/3p4/3P4/8/PPP1PPPP/RNBQKBNR w KQkq - 0 2",
                move = "g1f3",
                expected = "rnbqkbnr/ppp1pppp/8/3p4/3P4/5N2/PPP1PPPP/RNBQKB1R b"
            ),
            FenMoveCase(
                from = "rnbqkbnr/ppp1pppp/8/3p4/3P4/5N2/PPP1PPPP/RNBQKB1R b",
                move = "e7e6",
                expected = "rnbqkbnr/ppp2ppp/4p3/3p4/3P4/5N2/PPP1PPPP/RNBQKB1R w"
            ),
        )

        for (case in cases) {
            val fenFrom = FenNotation.fromFenString(case.from)
            val actual = fenFrom.move(case.move)
            val expected = FenNotation.fromFenString(case.expected)

            assertEquals(expected, actual)
        }
    }
}

private data class FenMoveCase(
    val from: String,
    val move: String,
    val expected: String
)