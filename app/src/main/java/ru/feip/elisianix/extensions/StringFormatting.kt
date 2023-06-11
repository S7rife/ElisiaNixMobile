package ru.feip.elisianix.extensions

import android.text.style.StrikethroughSpan
import android.widget.TextView
import androidx.core.text.toSpannable
import ru.feip.elisianix.R
import ru.feip.elisianix.remote.models.SizeMap


fun TextView.inCurrency(price: Double) {
    val text = "${String.format("%.3f", price)} ${this.resources.getString(R.string.currency)}"
    this.text = text
}

fun TextView.inStockUnits(cnt: Int) {
    val text = "$cnt ${this.resources.getString(R.string.stock_unit)}"
    this.text = text
}

fun TextView.addStrikethrough() {
    val text = this.text
    val spannable = text.toSpannable()
    spannable.setSpan(StrikethroughSpan(), 0, text.length, 0)
    this.text = spannable
}

fun TextView.sizeFormat(value: String, BreakLine: Boolean = false) {
    val sep = if (BreakLine) "\n" else " "
    val sizes = SizeMap.valueOf(value).sizes
    val size = "$value$sep(${sizes.first}-${sizes.second})"
    this.text = size
}