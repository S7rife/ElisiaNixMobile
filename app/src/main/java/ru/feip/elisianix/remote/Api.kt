package ru.feip.elisianix.remote

import retrofit2.http.*
import ru.feip.elisianix.remote.models.*


interface Api {


    ////////////////////////////////////////__CATALOG__////////////////////////////////////////////


    @GET("api/categories")
    suspend fun getCategories(): List<CategoryResponse>

}