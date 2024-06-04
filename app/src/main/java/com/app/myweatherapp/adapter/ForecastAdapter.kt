package com.app.myweatherapp.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.myweatherapp.R
import com.app.myweatherapp.databinding.ForecastItemBinding
import com.app.myweatherapp.model.Hour
import com.app.myweatherapp.utill.timeFormatter

class ForecastAdapter(private val list: List<Hour>) :
    RecyclerView.Adapter<ForecastAdapter.ViewHolder>() {
    class ViewHolder(private val binding: ForecastItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Hour) {
            Log.d("Forecast",item.time)
            binding.txtTime.text = timeFormatter(item.time)
            binding.txtTemp.text = item.temp_c.toInt().toString() + "°C"
            when (item.condition.text.trim().lowercase()) {
                "sunny" -> binding.forecastImage.setImageResource(R.drawable.icon_sunny)
                "clear" -> binding.forecastImage.setImageResource(R.drawable.icon_weather_cloud_sun)
                "partly cloudy" -> binding.forecastImage.setImageResource(R.drawable.icon_weather_cloud_sun)
                "mist" -> binding.forecastImage.setImageResource(R.drawable.icon_weather_cloud)
                "overcast" -> binding.forecastImage.setImageResource(R.drawable.icon_weather_cloud)
                "patchy rain nearby" -> binding.forecastImage.setImageResource(R.drawable.icon_weather_rain_cloud)
                "light rain" -> binding.forecastImage.setImageResource(R.drawable.icon_weather_rain_cloud)
                "patchy light rain in area with thunder" -> binding.forecastImage.setImageResource(R.drawable.icon_weather_thunderstorm_cloud)
                "patchy light drizzle" -> binding.forecastImage.setImageResource(R.drawable.icon_weather_thunderstorm_cloud)
                "snow" -> binding.forecastImage.setImageResource(R.drawable.icon_weather_snow_cloud)

                else -> binding.forecastImage.setImageResource(R.drawable.icon_weather_cloud)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ForecastItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }
}