package ru.feip.elisianix.remote.models

import com.google.gson.annotations.SerializedName


data class ProductCategory(
    @SerializedName("id")
    var id: Int,

    @SerializedName("name")
    var name: String,

    @SerializedName("alias")
    var alias: String,

    @SerializedName("image")
    var image: String,

    @SerializedName("isVisible")
    var isVisible: Boolean,
)

data class Brand(
    @SerializedName("id")
    var id: Int,

    @SerializedName("name")
    var name: String,

    @SerializedName("logo")
    var logo: String?,
)

data class Color(
    @SerializedName("id")
    var id: Int,

    @SerializedName("name")
    var name: String,

    @SerializedName("value")
    var value: String,
)

data class ActualSection(
    @SerializedName("id")
    var id: Int,

    @SerializedName("name")
    var name: String,

    @SerializedName("products")
    var products: List<ProductMainPreview>,
)

data class Image(
    @SerializedName("id")
    var id: Int,

    @SerializedName("name")
    var name: String?,

    @SerializedName("url")
    var url: String,
)