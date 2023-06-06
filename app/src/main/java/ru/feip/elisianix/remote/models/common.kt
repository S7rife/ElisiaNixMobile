package ru.feip.elisianix.remote.models

import com.google.gson.annotations.SerializedName


data class Brand(
    @SerializedName("id")
    var id: Int,

    @SerializedName("name")
    var name: String,
)

data class ProductColor(
    @SerializedName("id")
    var id: Int,

    @SerializedName("name")
    var name: String,

    @SerializedName("value")
    var value: String,
)

data class MainBlock(
    @SerializedName("id")
    var id: Int,

    @SerializedName("name")
    var name: String,

    @SerializedName("products")
    var products: List<ProductMainPreview>,

    @SerializedName("tag")
    var tag: String?,
)

data class Image(
    @SerializedName("id")
    var id: Int,

    @SerializedName("name")
    var name: String?,

    @SerializedName("url")
    var url: String,
)

data class Size(
    @SerializedName("id")
    var id: Int,

    @SerializedName("value")
    var value: String,

    @SerializedName("description")
    var description: String,

    @SerializedName("available")
    var available: Int,

    @SerializedName("onFitting")
    var onFitting: Boolean,

    @SerializedName("subscribed")
    var subscribed: Boolean,
)

enum class SizeMap(val sizes: Pair<Int, Int>) {
    XS(Pair(38, 40)),
    S(Pair(40, 42)),
    M(Pair(42, 44)),
    L(Pair(46, 48)),
    XL(Pair(50, 52)),
    XXL(Pair(54, 56))
}

data class Category(
    @SerializedName("id")
    var id: Int,

    @SerializedName("name")
    var name: String,

    @SerializedName("image")
    var image: Image
)