package com.app.myweatherapp.network

import com.app.myweatherapp.model.WeatherForecastResponse
import com.app.myweatherapp.model.WeatherResponse
import com.app.myweatherapp.model.WeatherSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ApiService {
    @GET("current.json")
    suspend fun getRealTimeWeather(
        @Query("q") q: String,
        @Header("X-RapidAPI-Key") key:String,
        @Header("X-RapidAPI-Host") host:String,
    ): Response<WeatherResponse>

    @GET("search.json")
    suspend fun getSearchWeather(
        @Query("q") q:String,
        @Header("X-RapidAPI-Key") key:String,
        @Header("X-RapidAPI-Host") host:String,
    ): Response<WeatherSearchResponse>

    @GET("forecast.json")
    suspend fun getForeCastWeather(
        @Query("q") q:String,
        @Query("days") days:String,
        @Header("X-RapidAPI-Key") key:String,
        @Header("X-RapidAPI-Host") host:String,
    ): Response<WeatherForecastResponse>

}