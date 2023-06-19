package ru.feip.elisianix.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.feip.elisianix.common.App
import ru.feip.elisianix.common.BASE_URL
import ru.feip.elisianix.remote.interceptors.AuthInterceptor
import java.util.concurrent.TimeUnit


class NetworkService {

    private val httpClient =
        OkHttpClient.Builder()
            .connectTimeout(10L, TimeUnit.SECONDS)
            .readTimeout(10L, TimeUnit.SECONDS)
            .writeTimeout(10L, TimeUnit.SECONDS)
            .callTimeout(10L, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                setLevel(HttpLoggingInterceptor.Level.BODY)
            })
            .addInterceptor(AuthInterceptor(App.sharedPreferences))
            .build()

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

sealed class Result<T> {
    class Success<T>(val result: T) : Result<T>()
    class Error<T>(val e: Throwable) : Result<T>()
}