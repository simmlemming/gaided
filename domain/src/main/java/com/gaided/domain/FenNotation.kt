package com.gaided.domain

public data class FenNotation(
    private val position: String,
    private val nextMoveColor: String
) {

    public fun move(move: MoveNotation): FenNotation {
        val fullTable = toFullTable(position)
        val from = move.take(2)
        val to = move.takeLast(2)

        val fromFile = FILES.indexOf(from[0])
        val fromRank = from[1].digitToInt()
        check(fromFile in 1..8)
        check(fromRank in 1..8)

        val toFile = FILES.indexOf(to[0])
        val toRank = to[1].digitToInt()
        check(toFile in 1..8)
        check(toRank in 1..8)

        val piece = fullTable[8 - fromRank][fromFile - 1]
        check(piece != "_")

        fullTable[8 - fromRank][fromFile - 1] = "_"
        fullTable[8 - toRank][toFile - 1] = piece

        val nextMove = if (nextMoveColor == "w") "b" else "w"
        return FenNotation(toFenPosition(fullTable), nextMove)
    }

    public companion object {
        private val LEGAL_NEXT_MOVES_COLORS = setOf("w", "b")
        private const val FILES = "_abcdefgh"

        public fun fromFenString(fen: String): FenNotation {
            val (position, nextMoveColor) = fen.split(" ")
            require(nextMoveColor in LEGAL_NEXT_MOVES_COLORS)
            return FenNotation(position, nextMoveColor)
        }

        private fun toFenPosition(fullTable: List<List<String>>): String {
            val fenPosition = mutableListOf<String>()

            for (rank in fullTable) {
                var spaceCount = 0
                val fenRank = StringBuilder()
                for (piece in rank) {
                    when (piece) {
                        "_" -> spaceCount++
                        else -> {
                            if (spaceCount > 0) fenRank.append(spaceCount)
                            fenRank.append(piece)
                            spaceCount = 0
                        }
                    }
                }
                if (spaceCount > 0) fenRank.append(spaceCount)
                fenPosition.add(fenRank.toString())
            }

            return fenPosition.joinToString(separator = "/")
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