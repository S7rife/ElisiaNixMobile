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

    @SerializedName("isNew")
    var isNew: Boolean,

    @SerializedName("brand")
    var brand: Brand,

    @SerializedName("images")
    var images: List<Image>,

    @SerializedName("color")
    var color: Color,

    @SerializedName("discount")
    var discount: Double?,

    @SerializedName("sizes")
    var sizes: List<Size>,

    @SerializedName("createdDate")
    var createdDate: String,
)

data class CategoryMainPreview(
    @SerializedName("id")
    var id: Int,

    @SerializedName("name")
    var name: String,

    @SerializedName("image")
    var image: Image,

    @SerializedName("subCategories")
    var subCategories: List<CategoryMainPreview>?,

    @SerializedName("level")
    var level: Int,

    @SerializedName("isVisible")
    var isVisible: Boolean,
)

data class ProductMainPreviews(
    @SerializedName("productsCount")
    var productsCount: Int,

    @SerializedName("minPrice")
    var minPrice: Double,

    @SerializedName("maxPrice")
    var maxPrice: Double,

    @SerializedName("products")
    var products: List<ProductMainPreview>
)