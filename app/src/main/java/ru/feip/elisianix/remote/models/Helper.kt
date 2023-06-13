package ru.feip.elisianix.remote.models

enum class SizeMap(val sizes: Pair<Int, Int>) {
    XS(Pair(38, 40)),
    S(Pair(40, 42)),
    M(Pair(42, 44)),
    L(Pair(46, 48)),
    XL(Pair(50, 52)),
    XXL(Pair(54, 56))
}

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