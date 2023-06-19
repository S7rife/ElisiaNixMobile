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

    @SerializedName("category")
    var category: Category,

    @SerializedName("images")
    var images: List<Image>,

    @SerializedName("colors")
    var colors: List<ProductColor>,

    @SerializedName("discount")
    var discount: Double?,

    @SerializedName("sizes")
    var sizes: List<Size>,

    @SerializedName("createdDate")
    var createdDate: String?,

    @SerializedName("inCart")
    var inCart: Boolean = false,

    @SerializedName("inFavorites")
    var inFavorites: Boolean = false,
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
    var products: List<ProductMainPreview>,

    @SerializedName("productProperties")
    var productProperties: List<ProductProperty>,

    )

data class ProductProperty(
    @SerializedName("id")
    var id: Int,

    @SerializedName("propertyName")
    var propertyName: String,

    @SerializedName("data")
    var data: String,
)

data class ProductDetail(
    @SerializedName("id")
    var id: Int,

    @SerializedName("name")
    var name: String,

    @SerializedName("article")
    var article: String?,

    @SerializedName("price")
    var price: Double,

    @SerializedName("description")
    var description: String?,

    @SerializedName("isNew")
    var isNew: Boolean,

    @SerializedName("colors")
    var colors: List<ProductColor>,

    @SerializedName("brand")
    var brand: Brand,

    @SerializedName("category")
    var category: Category,

    // don't work, have null
    @SerializedName("discount")
    var discount: Pair<String, String>?,

    @SerializedName("images")
    var images: List<Image>,

    @SerializedName("sizes")
    var sizes: List<Size>,

    @SerializedName("features")
    var features: List<ProductFeature>,

    @SerializedName("inCart")
    var inCart: Boolean = false,

    @SerializedName("inFavorites")
    var inFavorites: Boolean = false,
)

data class CartItemRemote(
    @SerializedName("id")
    var id: Int,

    @SerializedName("name")
    var name: String,

    @SerializedName("price")
    var price: Double,

    @SerializedName("article")
    var article: String?,

    @SerializedName("productId")
    var productId: Int,

    @SerializedName("productImage")
    var productImage: Image,

    @SerializedName("productColor")
    var productColor: ProductColor,

    @SerializedName("productSize")
    var productSize: Size,

    @SerializedName("brand")
    var brand: Brand,

    @SerializedName("category")
    var category: Category,

    @SerializedName("isLast")
    var isLast: Boolean,

    @SerializedName("count")
    var count: Int,

    @SerializedName("available")
    var available: Int,

    @SerializedName("inCart")
    var inCart: Boolean = false,

    @SerializedName("inFavorites")
    var inFavorites: Boolean = false,
)

data class Cart(
    @SerializedName("items")
    var items: List<CartItemRemote>,

    @SerializedName("itemsCount")
    var itemsCount: Int,

    @SerializedName("totalPrice")
    var totalPrice: Double,

    @SerializedName("discountPrice")
    var discountPrice: Double,

    @SerializedName("finalPrice")
    var finalPrice: Double,
)