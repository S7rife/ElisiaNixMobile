package ru.feip.elisianix.remote.models

import com.google.gson.annotations.SerializedName


data class ProductMainPreview(
    @SerializedName("id")
    var id: Int,

    @SerializedName("article")
    var article: String?,

    @SerializedName("name")
    var name: String?,

    @SerializedName("price")
    var price: Double,

    @SerializedName("countAvailable")
    var countAvailable: Int,

    @SerializedName("previewImage")
    var previewImage: String,

    @SerializedName("newProduct")
    var newProduct: Boolean,

    @SerializedName("category")
    var category: ProductCategory,

    @SerializedName("brand")
    var brand: Brand,
)

data class CategoryMainPreview(
    @SerializedName("id")
    var id: Int,

    @SerializedName("name")
    var name: String,

    @SerializedName("alias")
    var alias: String,

    @SerializedName("image")
    var image: Image,

    @SerializedName("subCategories")
    var subCategories: List<CategoryMainPreview>?,

    @SerializedName("level")
    var level: Int,

    @SerializedName("isVisible")
    var isVisible: Boolean,
)