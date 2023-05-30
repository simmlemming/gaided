@file:Suppress("SpellCheckingInspection")

package gaided

import com.gaided.domain.FenNotation
import org.junit.Assert.assertEquals
import org.junit.Test

internal class FenNotationTest {

    @Test
    fun pieceAt() {
        val fen =
            FenNotation.fromFenString("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")

        assertEquals("B", fen.pieceAt("c1"))
        assertEquals("P", fen.pieceAt("g2"))
        assertEquals("p", fen.pieceAt("g7"))
        assertEquals(null, fen.pieceAt("g3"))
    }
}
