package ru.feip.elisianix.remote

import retrofit2.http.*
import ru.feip.elisianix.remote.models.*


interface Api {


    ////////////////////////////////////////__CATALOG__////////////////////////////////////////////


    @GET("api/categories")
    suspend fun getCategories(): List<CategoryMainPreview>

    @GET("api/products")
    suspend fun getProducts(@QueryMap options: Map<String, String>): List<ProductMainPreview>

}