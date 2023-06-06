package ru.feip.elisianix.extensions

fun Double.inCurrency(currency: String): String {
    return String.format("%.3f", this) + currency
}