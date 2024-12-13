package de.felixlf.gradingscale2.utils

import kotlin.math.pow
import kotlin.math.roundToInt

internal fun Double.stringWithDecimals(decimals: Int = 2): String {
    val factor = 10.0.pow(decimals)
    return ((this * factor).roundToInt() / factor).toString()
}
