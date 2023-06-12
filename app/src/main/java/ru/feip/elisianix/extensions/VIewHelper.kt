package ru.feip.elisianix.extensions

import android.text.style.StrikethroughSpan
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.toSpannable
import com.google.android.material.button.MaterialButton
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

fun ImageView.setCartStatus(inCart: Boolean, large: Boolean = false) {
    val newImg = when (inCart) {
        false ->
            when (large) {
                false -> R.drawable.ic_to_cart_32
                true -> R.drawable.ic_to_cart_36
            }

        true -> when (large) {
            false -> R.drawable.ic_to_cart_32_in
            true -> R.drawable.ic_to_cart_36_in
        }
    }
    this.setImageDrawable(ContextCompat.getDrawable(this.context, newImg))
}

fun MaterialButton.withColors(activated: Boolean) {
    var black = resources.getColor(R.color.black, context?.theme)
    var white = resources.getColor(R.color.white, context?.theme)

    if (activated) black = white.also { white = black }

    this.setTextColor(white)
    this.setBackgroundColor(black)
}