package com.app.myweatherapp.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://weatherapi-com.p.rapidapi.com/"

    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        OkHttpClient.Builder().addInterceptor(loggingInterceptor).build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder().baseUrl(BASE_URL).client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()).build()
    }

    val apiService : ApiService by lazy{
        retrofit.create(ApiService::class.java)
    }

}