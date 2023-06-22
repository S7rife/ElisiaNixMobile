package ru.feip.elisianix.remote.models

import android.content.Context
import android.os.Bundle
import androidx.core.os.bundleOf
import ru.feip.elisianix.R
import ru.feip.elisianix.common.db.CartItem

fun Boolean.toInt() = if (this) 1 else 0

enum class SizeMap(val sizes: Pair<Int, Int>) {
    XS(Pair(38, 40)),
    S(Pair(40, 42)),
    M(Pair(42, 44)),
    L(Pair(46, 48)),
    XL(Pair(50, 52)),
    XXL(Pair(54, 56))
}

fun SizeMap.last(): String {
    return "${this.sizes.second}" + " (${this.name})"
}

val allSizes: List<SizeMap> = listOf(
    SizeMap.XS, SizeMap.S, SizeMap.M, SizeMap.L, SizeMap.XL, SizeMap.XXL
)

enum class SortMethod(val value: Triple<Int, String?, String>) {
    NEWEST(Triple(0, "Newest", "sort newest")),
    PRICE_ASC(Triple(1, "ByPriceAsc", "sorting by price ascending")),
    PRICE_DESC(Triple(2, "ByPriceDesc", "sorting by price descending")),
}

fun SortMethod.getFromLocale(context: Context): String {
    val a = this.value.second
    val resourceId: Int =
        context.resources.getIdentifier(
            this.value.second!!.lowercase(),
            "string",
            context.packageName
        )
    return context.resources.getString(resourceId)
}

val sortMethods: List<SortMethod> = listOf(
    SortMethod.NEWEST,
    SortMethod.PRICE_ASC,
    SortMethod.PRICE_DESC
)

data class SearchSettings(
    var safe: Boolean = true,
    var query: String? = null,
    var categoryId: Int? = null,
    var brandId: Int? = null,
    var sortMethod: SortMethod = SortMethod.NEWEST,
)

data class MainBlock(
    var id: Int,
    var name: String,
    var products: List<ProductMainPreview>,
    var tag: String?,
)

data class ActualBlocks(
    var new: ProductMainPreviews? = null,
    var discount: ProductMainPreviews? = null,
)

data class ImageProvider(
    var productId: Int,
    var categoryId: Int,
    var image: Image,
)

fun <T> toCartDialogData(item: T): Bundle? {
    when (item) {
        is ProductMainPreview -> return bundleOf(
            "product_id" to item.id,
            "size_ids" to item.sizes.map { it.id.toString() },
            "available_sizes" to item.sizes.map { it.value },
            "color_ids" to item.colors.map { it.id.toString() },
            "color_names" to item.colors.map { it.name },
            "color_values" to item.colors.map { it.value },
        )

        is ProductDetail -> return bundleOf(
            "product_id" to item.id,
            "size_ids" to item.sizes.map { it.id.toString() },
            "available_sizes" to item.sizes.filter { it.available > 0 }.map { it.value },
            "color_ids" to item.colors.map { it.id.toString() },
            "color_names" to item.colors.map { it.name },
            "color_values" to item.colors.map { it.value },
        )

        else -> return null
    }
}

val emptyAuthBundle = bundleOf("from_cart" to false)

fun sortPreviewsItems(items: List<ProductMainPreview>): List<ProductMainPreview> {
    return items.sortedWith(
        compareByDescending<ProductMainPreview> { it.name }.thenByDescending { it.id })
}

fun pickupPointParse(point: PickupPoint): PickupPoint {
    val parts = point.coordinates.split("\\s".toRegex())
    return point.copy(
        cooParse = Pair(parts[0].toDouble(), parts[1].toDouble()),
        workHours = point.workHours.map {
            it.copy(to = it.to.dropLast(3), from = it.from.dropLast(3))
        }
    )
}

fun Context.parseDays(point: PickupPoint): PickupPoint {
    val parsed = point.copy(
        workHours = point.workHours.map {
            it.copy(day = parseDayOfWeek(it.day, this))
        }
    )
    return parsed.copy(dayHoursOneLine = daysHoursToLine(parsed))
}

fun parseDayOfWeek(day: String, context: Context): String {
    val resourceId: Int =
        context.resources.getIdentifier(day.lowercase(), "string", context.packageName)
    return context.resources.getString(resourceId)
}

fun daysHoursToLine(point: PickupPoint): String {
    fun WorkHours.toKey() = Pair(from, to)

    val b = point.workHours.groupBy { it.toKey() }

    val line = b.map { map ->
        when (map.value.size) {
            1 -> "${map.value.first().day}: ${map.key.first} - ${map.key.second}"
            else -> "${map.value.first().day}-${map.value.last().day}: ${map.key.first} - ${map.key.second}"
        }
    }
    return line.joinToString(separator = ", ")
}

fun Context.getEmptyError(field: CharSequence?): String {
    return "$field ${this.getString(R.string.empty_error)}"
}

fun Cart.contains(item: CartItem): Boolean {
    if (this.items == null) return false
    return Triple(item.productId, item.colorId, item.sizeId) in this.items!!.map {
        Triple(it.productId, it.productColor.id, it.productSize.id)
    }
}