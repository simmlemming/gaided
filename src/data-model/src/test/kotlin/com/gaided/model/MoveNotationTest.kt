package com.gaided.model

import org.junit.Assert.assertEquals
import org.junit.Test


class MoveNotationTest {

    @Test
    fun `valid input`() {
        assertEquals(
            MoveNotation(from = "a1", to = "b4", promoteToPiece = null),
            MoveNotation.fromString("a1b4")
        )

        assertEquals(
            MoveNotation(from = "a1", to = "b4", promoteToPiece = 'q'),
            MoveNotation.fromString("a1b4q")
        )
    }
}