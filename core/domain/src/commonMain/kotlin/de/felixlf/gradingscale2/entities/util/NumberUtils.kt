package de.felixlf.gradingscale2.entities.util

import kotlin.math.abs

/**
 * Extension function for Double to convert it to a String with a specific number of decimal places.
 * The default number of decimal places is 2.
 * If the number is a whole number, the decimal places are removed.
 * If the number is a whole number and [removeTrailingZeros] is set to false, the decimal places are kept.
 */
fun Double.stringWithDecimals(decimals: Int = 2, removeTrailingZeros: Boolean = true): String {
    val safeDecimals = decimals.coerceAtLeast(0)
    val zeroResult = if (removeTrailingZeros || safeDecimals == 0) "0" else "0.${"0".repeat(safeDecimals)}"
    if (!isFinite() || this == 0.0) return zeroResult

    val isNegative = this < 0.0
    val absVal = abs(this)
    if (absVal == 0.0) return zeroResult

    val str = absVal.toString()
    val eIndex = str.indexOfFirst { it == 'e' || it == 'E' }
    val mantissa = if (eIndex >= 0) str.substring(0, eIndex) else str
    val exp = if (eIndex >= 0) str.substring(eIndex + 1).toInt() else 0

    val dotIndex = mantissa.indexOf('.')
    val rawDigits: String
    val fracLen: Int
    if (dotIndex >= 0) {
        val intPart = mantissa.substring(0, dotIndex)
        val fracPart = mantissa.substring(dotIndex + 1)
        rawDigits = intPart + fracPart
        fracLen = fracPart.length
    } else {
        rawDigits = mantissa
        fracLen = 0
    }

    val rawIntLen = rawDigits.length - fracLen + exp
    if (rawIntLen + safeDecimals < 0) return zeroResult

    val normDigits: String
    val normIntLen: Int
    if (rawIntLen <= 0) {
        normDigits = "0".repeat(1 - rawIntLen) + rawDigits
        normIntLen = 1
    } else {
        normDigits = rawDigits
        normIntLen = rawIntLen
    }

    val cutIndex = normIntLen + safeDecimals
    val roundUp = cutIndex < normDigits.length && normDigits[cutIndex] >= '5'
    val kept = CharArray(cutIndex) { idx ->
        if (idx < normDigits.length) normDigits[idx] else '0'
    }

    var carryOverflow = false
    if (roundUp) {
        var carry = 1
        var i = kept.lastIndex
        while (i >= 0 && carry > 0) {
            val sum = (kept[i] - '0') + carry
            kept[i] = '0' + (sum % 10)
            carry = sum / 10
            i--
        }
        carryOverflow = carry > 0
    }

    val digitsStr = if (carryOverflow) "1" + kept.concatToString() else kept.concatToString()
    val finalIntLen = if (carryOverflow) normIntLen + 1 else normIntLen

    if (digitsStr.all { it == '0' }) return zeroResult

    val intPart = digitsStr.substring(0, finalIntLen).trimStart('0').ifEmpty { "0" }
    val rawFracPart = if (safeDecimals > 0) digitsStr.substring(finalIntLen) else ""
    val fracPart = if (removeTrailingZeros) rawFracPart.trimEnd('0') else rawFracPart

    val sign = if (isNegative) "-" else ""
    return if (fracPart.isEmpty()) "$sign$intPart" else "$sign$intPart.$fracPart"
}

/**
 * Parses a numeric string accepting either '.' or ',' as decimal separator.
 * Returns null for blank, malformed, or non-finite values (NaN / Infinity).
 */
fun String.toLocaleDoubleOrNull(): Double? =
    trim().replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() }
