package ru.feip.elisianix.extensions

import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.toSpannable
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import ru.feip.elisianix.R
import ru.feip.elisianix.remote.models.SizeMap
import ru.feip.elisianix.remote.models.last


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

fun TextView.colorEnd(cntEnt: Int, color: Int, text: String) {
    val spannable = text.toSpannable()
    spannable.setSpan(ForegroundColorSpan(color), text.length - cntEnt, text.length, 0)
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

fun ImageView.setFavoriteStatus(inFavorites: Boolean) {
    val newImg = when (inFavorites) {
        true -> R.drawable.ic_favorite_in
        false -> R.drawable.ic_favorite
    }
    this.setImageDrawable(ContextCompat.getDrawable(this.context, newImg))
}

fun BottomNavigationView.setBadge(tabResId: Int, badgeValue: Int) {
    var value = badgeValue.toString()
    if (badgeValue > 99) value = "99+"
    getOrCreateCustomBadge(this, tabResId)?.let { badge ->
        badge.isVisible = badgeValue > 0
        badge.text = value
    }
}

private fun getOrCreateCustomBadge(bottomBar: BottomNavigationView, tabResId: Int): TextView? {
    val parentView = bottomBar.findViewById<ViewGroup>(tabResId)
    return parentView?.let {
        var badge = parentView.findViewById<TextView>(R.id.menuItemBadge)
        if (badge == null) {
            LayoutInflater.from(parentView.context).inflate(R.layout.nav_badge, parentView, true)
            badge = parentView.findViewById(R.id.menuItemBadge)
        }
        badge
    }
}

fun MaterialCardView.setStrokeSelector(isVisible: Boolean) {
    var black = resources.getColor(R.color.black, context?.theme)
    var transparent = resources.getColor(R.color.transparent, context?.theme)

    if (!isVisible) black = transparent.also { transparent = black }
    this.strokeColor = black
}

fun RecyclerView.smoothScrollToTop() {
    this.layoutManager?.smoothScrollToPosition(this, null, 0)
}

fun buildTableOfSizes(): List<MutableList<String>> {
    return listOf(
        mutableListOf(SizeMap.XS.last(), "78-82", "60-64", "86-90"),
        mutableListOf(SizeMap.S.last(), "82-86", "64-68", "90-94"),
        mutableListOf(SizeMap.M.last(), "86-90", "68-72", "94-98"),
        mutableListOf(SizeMap.L.last(), "90-94", "72-76", "98-102"),
        mutableListOf(SizeMap.XL.last(), "94-98", "76-80", "102-106"),
        mutableListOf(SizeMap.XXL.last(), "98-102", "80-84", "106-110"),
    )
}