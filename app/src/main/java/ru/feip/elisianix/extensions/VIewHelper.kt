package ru.feip.elisianix.extensions

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.text.toSpannable
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import ru.feip.elisianix.R
import ru.feip.elisianix.remote.models.SizeMap
import ru.feip.elisianix.remote.models.last
import ru.feip.elisianix.remote.models.toInt
import java.text.NumberFormat
import java.util.Locale


fun TextView.inCurrency(price: Double) {
    val m = price.toInt()
    val myNumber = NumberFormat.getNumberInstance(Locale.US)
        .format(m)
        .replace(",", " ")
    val text = "$myNumber ${this.resources.getString(R.string.currency)}"
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

fun TextView.setSelectorPaint(selected: Boolean) {
    val p = Paint()
    when (selected) {
        true -> {
            p.flags = Paint.UNDERLINE_TEXT_FLAG
            p.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        false -> {
            p.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
    }
    this.paintFlags = p.flags
    this.paint.typeface = p.typeface
}

fun TextView.setUnderline() {
    val p = Paint()
    p.flags = Paint.UNDERLINE_TEXT_FLAG
    this.paintFlags = p.flags
}

fun TextView.addUnderBoldPart(bold: String) {
    val extra = bold.toSpannable()
    extra.setSpan(UnderlineSpan(), 0, extra.length, 0)
    extra.setSpan(StyleSpan(Typeface.BOLD), 0, extra.length, 0)
    extra.setSpan(ForegroundColorSpan(Color.BLACK), 0, extra.length, 0)
    this.text = TextUtils.concat(this.text, " ", extra)
}

fun TextView.addRegularPart(regular: String) {
    val extra = regular.toSpannable()
    this.text = TextUtils.concat(this.text, " ", extra)
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

fun TextView.setContrastText(color: Int, text: String) {
    var black = Color.BLACK
    var white = Color.WHITE

    val contrast = ColorUtils.calculateContrast(color, white)
    if (contrast > 2) black = white.also { white = black }

    this.setTextColor(black)
    this.text = text
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

fun MaterialButton.withColors(activated: Boolean, enabled: Boolean = true) {
    when (enabled) {
        false -> this.isEnabled = false
        true -> {
            var black = Color.BLACK
            var white = Color.WHITE

            if (activated) black = white.also { white = black }
            this.setBackgroundColor(black)
            this.setTextColor(white)
        }
    }
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
    this.strokeColor = Color.BLACK * isVisible.toInt()
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

fun RecyclerView.disableAnimation() {
    val anim = this.itemAnimator as SimpleItemAnimator
    anim.supportsChangeAnimations = false.also { this.itemAnimator = anim }
}