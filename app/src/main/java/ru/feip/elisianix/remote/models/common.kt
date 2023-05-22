package ru.feip.elisianix.remote.models

import com.google.gson.annotations.SerializedName


data class Brand(
    @SerializedName("id")
    var id: Int,

    @SerializedName("name")
    var name: String,
)

data class Color(
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

    @SerializedName("available")
    var available: Int,

    @SerializedName("onFitting")
    var onFitting: Boolean,

    @SerializedName("subscribed")
    var subscribed: Boolean,
)