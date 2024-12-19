package de.felixlf.gradingscale2.utils

import kotlin.math.pow
import kotlin.math.roundToInt

internal fun Double.stringWithDecimals(decimals: Int = 2, removeTrailingZeros: Boolean = true): String {
    val factor = 10.0.pow(decimals)
    val roundedValue = ((this * factor).roundToInt() / factor).toString()
    return if (removeTrailingZeros && roundedValue.endsWith(".0")) roundedValue.substringBeforeLast(".0") else roundedValue
}
