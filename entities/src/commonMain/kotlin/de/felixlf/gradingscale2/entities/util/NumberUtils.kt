package de.felixlf.gradingscale2.entities.util

import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Extension function for Double to convert it to a String with a specific number of decimal places.
 * The default number of decimal places is 2.
 * If the number is a whole number, the decimal places are removed.
 * If the number is a whole number and [removeTrailingZeros] is set to false, the decimal places are kept.
 */
fun Double.stringWithDecimals(decimals: Int = 2, removeTrailingZeros: Boolean = true): String {
    val factor = 10.0.pow(decimals)
    val roundedValue = ((this * factor).roundToInt() / factor).toString()
    return if (removeTrailingZeros && roundedValue.endsWith(".0")) roundedValue.substringBeforeLast(".0") else roundedValue
}
