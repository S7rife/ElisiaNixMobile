package ru.feip.elisianix.remote

import retrofit2.http.*
import ru.feip.elisianix.common.db.UserInfo
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


    @POST("api/order")
    suspend fun toOrder(@Body order: RequestOrder): Int


    //////////////////////////////////////////__AUTH__/////////////////////////////////////////////


    @POST("api/account/auth/call")
    suspend fun sendPhoneNumber(@Body body: RequestAuthSendPhoneNumber)

    @POST("api/account/auth")
    suspend fun sendAuthCode(@Body body: RequestAuthSendCode): UserInfo


    ///////////////////////////////////////////__MAP__/////////////////////////////////////////////


    @GET("api/pickupPoints")
    suspend fun getPickupPoints(): List<PickupPoint>
}