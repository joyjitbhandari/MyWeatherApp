package com.app.myweatherapp.repository

import com.app.myweatherapp.model.WeatherForecastResponse
import com.app.myweatherapp.model.WeatherResponse
import com.app.myweatherapp.model.WeatherSearchResponse
import com.app.myweatherapp.network.ApiService
import com.app.myweatherapp.utill.Constance.HOST
import com.app.myweatherapp.utill.Constance.KEY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class Repository(private val apiService: ApiService) {

    suspend fun getRealTimeWeather(q: String): Response<WeatherResponse> {
        return withContext(Dispatchers.IO) { apiService.getRealTimeWeather(q, KEY, HOST) }
    }

    suspend fun getSearchWeather(q:String): Response<WeatherSearchResponse> {
        return withContext(Dispatchers.IO){ apiService.getSearchWeather(q,KEY,HOST)}
    }

    suspend fun getForecastWeather(q:String): Response<WeatherForecastResponse> {
        return withContext(Dispatchers.IO){ apiService.getForeCastWeather(q,"3",KEY,HOST)}
    }

}