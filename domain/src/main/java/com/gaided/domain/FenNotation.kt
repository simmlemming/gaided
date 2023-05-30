package com.gaided.domain

public data class FenNotation private constructor(
    public val fenString: String,
) {
    private val position: String = fenString.split(" ")[0]
    private val nextMoveColor: String = fenString.split(" ")[1]

    public fun pieceAt(square: SquareNotation): String? {
        val fullTable = toFullTable(position)

        val file = FILES.indexOf(square[0])
        val rank = square[1].digitToInt()
        check(file in 1..8)
        check(rank in 1..8)

        return fullTable[8 - rank][file - 1].takeIf { it != NULL_PIECE }
    }

    public companion object {
        private val LEGAL_NEXT_MOVES_COLORS = setOf("w", "b")
        private const val FILES = "_abcdefgh"
        private const val NULL_PIECE = "_"
        public val START_POSITION: FenNotation =
            FenNotation("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")

        public fun fromFenString(fen: String): FenNotation {
            val parts = fen.split(" ")
            require(parts.size == 6)

            val nextMoveColor = parts[1]
            require(nextMoveColor in LEGAL_NEXT_MOVES_COLORS)

            return FenNotation(fen)
        }

        private fun toFullTable(position: String): MutableList<MutableList<String>> {
            val ranks = position.split("/")
            check(ranks.size == 8)

            val fullTable = mutableListOf<MutableList<String>>()
            for (rank in ranks) {
                val fullRank = toFullRank(rank)
                fullTable.add(fullRank)
            }

            check(fullTable.size == 8)
            return fullTable
        }

        private fun toFullRank(rank: String): MutableList<String> {
            val files = rank.toCharArray()
            val fullRank = files.fold(mutableListOf<String>()) { acc, file ->
                when {
                    file.isDigit() -> {
                        repeat(file.digitToInt()) { acc.add("_") }
                        acc
                    }
                    else -> {
                        acc.add(file.toString())
                        acc
                    }
                }
            }

            check(fullRank.size == 8)
            return fullRank
        }
    }
}