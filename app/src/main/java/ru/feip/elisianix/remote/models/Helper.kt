package ru.feip.elisianix.remote.models

import android.os.Bundle
import androidx.core.os.bundleOf

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