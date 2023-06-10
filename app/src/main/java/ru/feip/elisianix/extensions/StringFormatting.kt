package ru.feip.elisianix.extensions

import android.text.style.StrikethroughSpan
import android.widget.TextView
import androidx.core.text.toSpannable

fun Double.inCurrency(currency: String): String {
    return String.format("%.3f", this) + currency
}

fun TextView.addStrikethrough(text: String) {
    val spannable = text.toSpannable()
    spannable.setSpan(StrikethroughSpan(), 0, text.length, 0)
    this.text = spannable
}