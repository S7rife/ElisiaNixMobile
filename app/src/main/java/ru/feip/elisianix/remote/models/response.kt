package ru.feip.elisianix.remote.models

import com.google.gson.annotations.SerializedName


data class ProductResponse(
    @SerializedName("id")
    var id: Int,

    @SerializedName("article")
    var article: String?,

    @SerializedName("alias")
    var alias: String?,

    @SerializedName("description")
    var description: String?,

    @SerializedName("price")
    var price: Int,

    @SerializedName("countAvailable")
    var countAvailable: Int,

    @SerializedName("previewImage")
    var previewImage: String,

    @SerializedName("isVisible")
    var isVisible: Boolean,

    @SerializedName("category")
    var category: ProductCategory,

    @SerializedName("brand")
    var brand: Brand,
)

data class CategoryResponse(
    @SerializedName("id")
    var id: Int,

    @SerializedName("name")
    var name: String,

    @SerializedName("alias")
    var alias: String,

    @SerializedName("image")
    var image: String,

    @SerializedName("subCategories")
    var subCategories: List<CategoryResponse>?,

    @SerializedName("level")
    var level: Int,

    @SerializedName("isVisible")
    var isVisible: Boolean,
)