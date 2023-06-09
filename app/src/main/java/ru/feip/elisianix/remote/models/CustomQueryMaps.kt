package ru.feip.elisianix.remote.models

import com.google.gson.annotations.SerializedName
import kotlin.reflect.full.memberProperties

inline fun <reified T : Any> T.dataClassToMap(): Map<String, String> {
    val queryMap = mutableMapOf<String, String>()
    for (prop in T::class.memberProperties) {
        val paramName = prop.name
        val paramValue = prop.get(this)
        paramValue?.let { queryMap.put(paramName, paramValue.toString()) }
    }
    return queryMap
}


data class ProductsQueryMap(
    @SerializedName("brands")
    // TODO put ids as list
    var brands: Int? = null,

    @SerializedName("sortMethod")
    var sortMethod: String? = null,

    @SerializedName("minPrice")
    var minPrice: Int? = null,

    @SerializedName("maxPrice")
    var maxPrice: Int? = null,

    @SerializedName("sizes")
    var sizes: List<Int>? = null,

    @SerializedName("categories")
    var categories: String? = null,

    @SerializedName("discount")
    var discount: Boolean? = null,

    @SerializedName("newProducts")
    var newProducts: Boolean? = null,

    @SerializedName("limit")
    var limit: Int? = null,

    @SerializedName("offset")
    var offset: Int? = null,

    @SerializedName("withProperties")
    var withProperties: Boolean? = null,

    @SerializedName("colors")
    var colors: List<Int>? = null,

    @SerializedName("filter")
    var filter: String? = null,

    @SerializedName("inSubCategories")
    var inSubCategories: Boolean? = null,

    @SerializedName("allPossibleProperties")
    var allPossibleProperties: Boolean? = null,

    @SerializedName("selectOnlyAvailableProducts")
    var selectOnlyAvailableProducts: Boolean? = null,
)