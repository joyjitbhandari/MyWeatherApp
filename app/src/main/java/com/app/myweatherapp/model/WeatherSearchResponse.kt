package com.app.myweatherapp.model

class WeatherSearchResponse : ArrayList<WeatherSearchResponseItem>()
data class WeatherSearchResponseItem(
    val country: String,
    val id: Int,
    val lat: Double,
    val lon: Double,
    val name: String,
    val region: String,
    val url: String
)