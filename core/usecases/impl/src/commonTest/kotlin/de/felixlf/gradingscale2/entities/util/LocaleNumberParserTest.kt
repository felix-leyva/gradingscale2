package de.felixlf.gradingscale2.entities.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LocaleNumberParserTest {

    @Test
    fun parsesDotAndCommaDecimalsAndTrimsWhitespace() {
        assertEquals(85.5, "85.5".toLocaleDoubleOrNull())
        assertEquals(85.5, "85,5".toLocaleDoubleOrNull())
        assertEquals(12.5, " 12,5 ".toLocaleDoubleOrNull())
        assertEquals(-4.25, "-4,25".toLocaleDoubleOrNull())
    }

    @Test
    fun returnsNullForBlankMalformedAndNonFiniteInputs() {
        listOf("", "   ", ".", ",", "-", "abc", "85,5,5", "85.5.5", "NaN", "Infinity", "-Infinity", "1e400").forEach { input ->
            assertNull(input.toLocaleDoubleOrNull(), "Expected null for '$input'")
        }
    }
}
