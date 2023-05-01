package gaided

import com.gaided.domain.Board
import com.gaided.domain.FenConverter
import com.gaided.domain.SquareNotation
import org.junit.Assert.assertEquals
import org.junit.Test

@Suppress("SpellCheckingInspection")
class FenConverterTest {
    @Test
    fun `from fen - starting position`() {
        val fenConverter = FenConverter()

        val whitePieces = position(1, "RNBQKBNR")
        val whitePawns = position(2, "PPPPPPPP")
        val blackPieces = position(8, "rnbqkbnr")
        val blackPawns = position(7, "pppppppp")
        val expectedPosition = whitePieces + whitePawns + blackPieces + blackPawns

        val actual =
            fenConverter.fromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")

        assertEquals(expectedPosition, actual)
    }

    @Test
    fun `from fen - random position`() {
        val fenConverter = FenConverter()

        val blackPieces = position(8, "rnbqkbnr")
        val blackPawns = mapOf(
            "a7" to Board.Piece('p'),
            "b7" to Board.Piece('p'),
            "c5" to Board.Piece('p'),
            "d7" to Board.Piece('p'),
            "e7" to Board.Piece('p'),
            "f7" to Board.Piece('p'),
            "g7" to Board.Piece('p'),
            "h7" to Board.Piece('p'),
        )

        val whitePawns = mapOf(
            "a2" to Board.Piece('P'),
            "b2" to Board.Piece('P'),
            "c2" to Board.Piece('P'),
            "d2" to Board.Piece('P'),
            "e4" to Board.Piece('P'),
            "f2" to Board.Piece('P'),
            "g2" to Board.Piece('P'),
            "h2" to Board.Piece('P'),
        )

        val whitePieces = mapOf(
            "a1" to Board.Piece('R'),
            "b1" to Board.Piece('N'),
            "c1" to Board.Piece('B'),
            "d1" to Board.Piece('Q'),
            "e1" to Board.Piece('K'),
            "f1" to Board.Piece('B'),
            "f3" to Board.Piece('N'),
            "h1" to Board.Piece('R'),
        )

        val expectedPosition = whitePieces + whitePawns + blackPieces + blackPawns

        val actual =
            fenConverter.fromFen("rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2")

        assertEquals(expectedPosition, actual)
    }

    private fun position(
        rankNumber: Int,
        fenRank: String
    ): Map<SquareNotation, Board.Piece> {
        val files = "abcdefgh"
        val squares = List(8) { "${files[it]}$rankNumber" as SquareNotation }
        val blackPieces = fenRank.toCharArray().map { Board.Piece(it) }

        return squares.zip(blackPieces).toMap()
    }
}