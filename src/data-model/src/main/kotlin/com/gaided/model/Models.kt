package com.gaided.model

public typealias SquareNotation = String
public typealias PieceNotation = Char

public data class MoveNotation(
    val from: SquareNotation,
    val to: SquareNotation,
    val promoteToPiece: Char? = null,
) {
    override fun toString(): String =
        listOfNotNull(from, to, promoteToPiece).joinToString(separator = "")

    public companion object {
        public fun fromString(move: String): MoveNotation = MoveNotation(
            from = "${move[0]}${move[1]}",
            to = "${move[2]}${move[3]}",
            promoteToPiece = move.getOrNull(4),
        )
    }
}

public fun String.toMove(): MoveNotation = MoveNotation.fromString(this)