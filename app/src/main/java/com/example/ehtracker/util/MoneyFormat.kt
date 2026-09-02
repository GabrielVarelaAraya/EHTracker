package com.example.ehtracker.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

/**
 * Formats money for display.
 * - < 10,000     -> grouped with commas + 2 decimals:  5,400.50
 * - >= 10,000    -> grouped with commas, no decimals: 1,293,495
 * - negatives keep sign: -2,500 / -1,293,495
 */
fun formatMoney(amount: Double): String {
    val symbols = DecimalFormatSymbols(Locale.US).apply {
        groupingSeparator = ','
        decimalSeparator = '.'
        minusSign = '-'
    }
    val pattern = if (abs(amount) >= 10_000) "#,##0" else "#,##0.00"
    return DecimalFormat(pattern, symbols).format(amount)
}

/**
 * Parses user-typed amounts like "1,293", "1 293.50", "$1,293" -> 1293.0
 * Strips spaces, commas, currency symbols, keeps digits and single dot.
 */
fun parseAmount(input: String): Double? {
    if (input.isBlank()) return null
    val cleaned = input.trim()
        .replace(" ", "")
        .replace(",", "")
        .replace("$", "")
        .replace("€", "")
        .replace("£", "")
    // allow only digits and dots, keep first dot
    var dotSeen = false
    val filtered = buildString {
        for (c in cleaned) {
            when {
                c.isDigit() -> append(c)
                c == '.' && !dotSeen -> { append(c); dotSeen = true }
                c == '-' && isEmpty() -> append(c)
            }
        }
    }
    return filtered.toDoubleOrNull()?.takeIf { it.isFinite() }
}

fun parseAmountOrZero(input: String): Double = parseAmount(input) ?: 0.0
