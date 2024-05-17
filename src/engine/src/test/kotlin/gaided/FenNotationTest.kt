@file:Suppress("SpellCheckingInspection")

package gaided

import com.gaided.engine.FenNotation
import org.junit.Assert.assertEquals
import org.junit.Test

internal class FenNotationTest {

    @Test
    fun pieceAt() {
        val fen =
            FenNotation.fromFenString("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")

        assertEquals('B', fen.pieceAt("c1"))
        assertEquals('P', fen.pieceAt("g2"))
        assertEquals('p', fen.pieceAt("g7"))
        assertEquals(null, fen.pieceAt("g3"))
    }

    @Test
    fun allPieces() {
        val fen =
            FenNotation.fromFenString("1n5r/p7/8/8/8/8/6P1/3Q4 w KQkq - 0 1")

        val expected = mapOf(
            "b8" to 'n',
            "h8" to 'r',
            "a7" to 'p',
            "g2" to 'P',
            "d1" to 'Q'
        )

        assertEquals(expected, fen.allPieces())
    }
}
