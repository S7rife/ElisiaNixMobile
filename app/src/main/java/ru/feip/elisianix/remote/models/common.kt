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

data class Category(
    @SerializedName("id")
    var id: Int,

    @SerializedName("name")
    var name: String,

    @SerializedName("image")
    var image: Image
)

data class ProductFeature(
    @SerializedName("value")
    var value: String,
)

data class PickupPoint(
    @SerializedName("id")
    var id: Int,

    @SerializedName("address")
    var address: String,

    @SerializedName("coordinates")
    var coordinates: String,

    @SerializedName("workHours")
    var workHours: List<WorkHours>,

    @SerializedName("cooParse")
    var cooParse: Pair<Double, Double>,

    @SerializedName("dayHoursOneLine")
    var dayHoursOneLine: String?
)

data class WorkHours(
    @SerializedName("from")
    var from: String,

    @SerializedName("to")
    var to: String,

    @SerializedName("day")
    var day: String,
)

data class Address(
    @SerializedName("index")
    var index: String,

    @SerializedName("city")
    var city: String,

    @SerializedName("street")
    var street: String,

    @SerializedName("house")
    var house: String,

    @SerializedName("flat")
    var flat: String?,
)