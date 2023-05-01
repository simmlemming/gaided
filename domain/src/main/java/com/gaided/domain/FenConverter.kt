package com.gaided.domain


@Suppress("SpellCheckingInspection")
internal class FenConverter {
    // "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

    fun fromFen(fen: FenString): Map<SquareNotation, Board.Piece> {
        val fenRanks = fen.split(' ')[0].split('/')
        val position = mutableMapOf<SquareNotation, Board.Piece>()

        fenRanks
            .reversed()
            .forEachIndexed { index, fenRank ->
                position += parseRank(index + 1, fenRank)
            }

        return position
    }

    private fun parseRank(rank: Int, fenRank: String): Map<SquareNotation, Board.Piece> {
        val position = mutableMapOf<SquareNotation, Board.Piece>()

        var file = 'a'
        for (fenSymbol in fenRank.toCharArray()) {
            when {
                fenSymbol.isDigit() -> {
                    file += fenSymbol.digitToInt()
                }
                else -> {
                    position["$file$rank"] = Board.Piece(fenSymbol)
                    file++
                }
            }
        }

        return position
    }
}