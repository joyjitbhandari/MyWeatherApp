package com.app.myweatherapp.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.myweatherapp.model.WeatherForecastResponse
import com.app.myweatherapp.model.WeatherResponse
import com.app.myweatherapp.model.WeatherSearchResponse
import com.app.myweatherapp.network.NetworkResponse
import com.app.myweatherapp.repository.Repository
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.json.JSONObject

class HomeViewModel(private val repository: Repository) : ViewModel() {
    val TAG = "response"

    private val realTimeWeatherResponseLiveData =
        MutableLiveData<NetworkResponse<WeatherResponse>>()
    val realTimeWeatherResponse get() = realTimeWeatherResponseLiveData

    private val searchWeatherResponseLiveData =
        MutableLiveData<NetworkResponse<WeatherSearchResponse>>()
    val searchWeatherResponse get() = searchWeatherResponseLiveData

    private val forecastWeatherResponseLiveData =
        MutableLiveData<NetworkResponse<WeatherForecastResponse>>()
    val forecastWeatherResponse get() = forecastWeatherResponseLiveData

    fun getRealTimeWeather(q: String) = viewModelScope.launch {
        realTimeWeatherResponseLiveData.value = NetworkResponse.Loading()
        try {
            val response = repository.getRealTimeWeather(q)
            if (response.isSuccessful) {
                realTimeWeatherResponseLiveData.value = NetworkResponse.Success(response.body()!!)
                Log.d(TAG, "getRealTimeWeather: ${response.body()!!}")
            } else {
                val errorObject = JSONObject(response.errorBody()!!.string())
                val errorData = errorObject.getJSONObject("error")
                realTimeWeatherResponseLiveData.value = NetworkResponse.Error(errorData.getString("message"))
                Log.d(TAG, "getRealTimeWeather: ${errorData.getString("code")}, ${errorData.getString("message")}")
            }
        } catch (e: Exception) {
            realTimeWeatherResponseLiveData.value = NetworkResponse.Error("Something went wrong")
            Log.d(TAG, "getRealTimeWeather: ${e.message}")
        }

    }

    fun getSearchWeather(q: String) = viewModelScope.launch {
        searchWeatherResponseLiveData.value = NetworkResponse.Loading()
        try {
            val response = repository.getSearchWeather(q)
            if (response.isSuccessful){
                if(response.body()!!.isEmpty()){
                    searchWeatherResponseLiveData.value = NetworkResponse.Error("No data found")
                }else{
                    searchWeatherResponseLiveData.value = NetworkResponse.Success(response.body()!!)
                    Log.d(TAG, "getSearchWeather: ${response.body()!!}")
                }
            }else{
                val errorObject = JSONObject(response.errorBody()!!.string())
                val errorData = errorObject.getJSONObject("error")
                searchWeatherResponseLiveData.value = NetworkResponse.Error(errorData.getString("message"))
            }

        }catch (e:Exception){
            searchWeatherResponseLiveData.value = NetworkResponse.Error("Something went wrong")
            Log.d(TAG, "getSearchWeather: ${e.message}")
        }
    }

    fun getForecastWeather(q: String) = viewModelScope.launch {
        forecastWeatherResponse.value = NetworkResponse.Loading()
        try {
            val response = repository.getForecastWeather(q)
            if (response.isSuccessful) {
                forecastWeatherResponse.value = NetworkResponse.Success(response.body()!!)
                Log.d(TAG, "getForecastWeather: ${response.body()!!}")
            } else {
                val errorObject = JSONObject(response.errorBody()!!.string())
                val errorData = errorObject.getJSONObject("error")
                forecastWeatherResponse.value = NetworkResponse.Error(errorData.getString("message"))
                Log.d(TAG, "getForecastWeather: ${errorData.getString("code")}, ${errorData.getString("message")}")
            }
        } catch (e: Exception) {
            forecastWeatherResponse.value = NetworkResponse.Error("Something went wrong")
            Log.d(TAG, "getForecastWeather: ${e.message}")
        }
    }

}