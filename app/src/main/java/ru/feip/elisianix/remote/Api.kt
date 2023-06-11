package ru.feip.elisianix.remote

import retrofit2.http.*
import ru.feip.elisianix.remote.models.*


interface Api {


    ////////////////////////////////////////__CATALOG__////////////////////////////////////////////


    @GET("api/categories")
    suspend fun getCategories(): List<CategoryMainPreview>

    @GET("api/products")
    suspend fun getProducts(@QueryMap(encoded = true) options: Map<String, String>): ProductMainPreviews

    @GET("api/products/{productId}")
    suspend fun getProductDetail(@Path("productId") productId: Int): ProductDetail

    @GET("api/products/{categoryId}/recs")
    suspend fun getProductRecs(@Path("categoryId") categoryId: Int): ProductMainPreviews


    ///////////////////////////////////////////__CART__////////////////////////////////////////////


    @POST("api/basket/basketInfo")
    suspend fun getCartNoAuth(@Body items: RequestCartItems): Cart
}