package de.felixlf.gradingscale2.entities.util

import kotlin.test.Test
import kotlin.test.assertEquals

class NumberUtilsTest {

    @Test
    fun stringWithDecimalsHandlesLargeNumbersWithoutIntOverflowOrScientificNotation() {
        assertEquals("25000000", 25_000_000.0.stringWithDecimals())
        assertEquals("10000000", 10_000_000.0.stringWithDecimals())
        assertEquals("25000000.75", 25_000_000.75.stringWithDecimals())
    }

    @Test
    fun stringWithDecimalsHandlesNonFiniteWithoutCrashing() {
        assertEquals("0", Double.NaN.stringWithDecimals())
        assertEquals("0.00", Double.NaN.stringWithDecimals(2, removeTrailingZeros = false))
        assertEquals("0", Double.POSITIVE_INFINITY.stringWithDecimals())
        assertEquals("0", Double.NEGATIVE_INFINITY.stringWithDecimals())
    }

    @Test
    fun stringWithDecimalsHonorsRemoveTrailingZerosAndRoundingHalfUp() {
        assertEquals("10.00", 10.0.stringWithDecimals(2, removeTrailingZeros = false))
        assertEquals("10.50", 10.5.stringWithDecimals(2, removeTrailingZeros = false))
        assertEquals("10", 10.0.stringWithDecimals(2, removeTrailingZeros = true))
        assertEquals("1.01", 1.005.stringWithDecimals(2))
        assertEquals("29", (0.29 * 100.0).stringWithDecimals(2))
        assertEquals("57", (0.57 * 100.0).stringWithDecimals(2))
        assertEquals("10.00", 9.999.stringWithDecimals(2, removeTrailingZeros = false))
        assertEquals("0", (-0.001).stringWithDecimals(2))
        assertEquals("-12.35", (-12.345).stringWithDecimals(2))
        assertEquals("0.01", 0.005.stringWithDecimals(2))
        assertEquals("0.05", 0.05.stringWithDecimals(2))
        assertEquals("0", 1e-7.stringWithDecimals(2))
        assertEquals("1", 0.999.stringWithDecimals(0))
    }
}
