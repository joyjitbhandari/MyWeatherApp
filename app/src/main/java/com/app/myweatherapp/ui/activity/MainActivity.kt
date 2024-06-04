package com.app.myweatherapp.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.app.myweatherapp.R
import com.app.myweatherapp.adapter.ForecastAdapter
import com.app.myweatherapp.databinding.ActivityMainBinding
import com.app.myweatherapp.network.NetworkResponse
import com.app.myweatherapp.network.RetrofitClient
import com.app.myweatherapp.repository.Repository
import com.app.myweatherapp.utill.InternetConnection
import com.app.myweatherapp.viewModel.HomeViewModel
import com.app.myweatherapp.viewModel.factory.ViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity(), OnClickListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val PERMISSSION_REQUEST_CODE = 123
    private var searchClick = false
    private var refreshClick = false
    private var updatedLocation = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        homeViewModel = ViewModelProvider(
            this, ViewModelFactory(Repository(RetrofitClient.apiService))
        )[HomeViewModel::class.java]
        initObserver()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fetchLocation()


        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val inputMethodManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.searchView.windowToken, 0)
                if (!query.isNullOrEmpty()) {
                    lifecycleScope.launch {
                        if (InternetConnection.isNetworkAvailable(this@MainActivity)) {
                            homeViewModel.getSearchWeather(query.trim())
                        } else {
                            Snackbar.make(
                                binding.root, "No internet connection.", Snackbar.LENGTH_INDEFINITE
                            ).setAction("Try again") {
                                showSnackBarAgain()
                            }
                        }
                    }
                } else Toast.makeText(
                    this@MainActivity, "Please enter a city name", Toast.LENGTH_SHORT
                ).show()

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })

    }

    private fun fetchLocation(q: String? = null) {
        if (q.isNullOrEmpty()) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                lifecycleScope.launch {
                    if (InternetConnection.isNetworkAvailable(this@MainActivity)) {
                        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                            if (location != null) {
                                val latitude = location.latitude
                                val longitude = location.longitude
                                val currentLocation = "$latitude,$longitude"
                                updatedLocation = currentLocation
                                Log.d("LOC", currentLocation)
                                homeViewModel.getRealTimeWeather(currentLocation)
                                homeViewModel.getForecastWeather("$latitude,$longitude")
                            } else {
                                Toast.makeText(
                                    this@MainActivity, "Location not found", Toast.LENGTH_SHORT
                                ).show()
                            }

                        }.addOnFailureListener {
                            val snackBar = Snackbar.make(
                                binding.root, "Something went wrong!", Snackbar.LENGTH_INDEFINITE
                            )
                            snackBar.setAction("Try again") {
                                fetchLocation()
                            }.show()
                        }
                    } else {
                        val snackBar = Snackbar.make(
                            binding.root, "No internet connection.", Snackbar.LENGTH_INDEFINITE
                        )
                        snackBar.setAction("Try again") {
                            showSnackBarAgain()
                        }.show()
                    }
                }
            } else {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ), PERMISSSION_REQUEST_CODE
                )
            }
        } else {
            lifecycleScope.launch {
                if (InternetConnection.isNetworkAvailable(this@MainActivity)) {
                    homeViewModel.getRealTimeWeather(q)
                    homeViewModel.getForecastWeather(q)
                } else {
                    Snackbar.make(
                        binding.root, "No internet connection.", Snackbar.LENGTH_INDEFINITE
                    ).setAction("Try again") {
                        showSnackBarAgain()
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                recreate()
            } else Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSnackBarAgain() {
        val snackBar = Snackbar.make(
            binding.root, "No internet connection.", Snackbar.LENGTH_INDEFINITE
        )
        snackBar.setAction("Try again") {
            lifecycleScope.launch {
                if (InternetConnection.isNetworkAvailable(this@MainActivity))
                    recreate()
                else
                    showSnackBarAgain()
            }
        }.show()
    }


    @SuppressLint("SetTextI18n")
    private fun initObserver() {
        homeViewModel.realTimeWeatherResponse.observe(this as LifecycleOwner) {
            when (it) {
                is NetworkResponse.Loading -> {
                    binding.simmerLayout.visibility = View.VISIBLE
                    binding.mainLayout.visibility = View.GONE
                }

                is NetworkResponse.Success -> {
                    binding.simmerLayout.visibility = View.GONE
                    binding.mainLayout.visibility = View.VISIBLE
                    setWeatherIcon(it.data?.current?.condition?.text.toString())
                    binding.txtLocationArea.text =
                        it.data?.location?.name.toString() + "," + it.data?.location?.region + "," + it.data?.location?.country
                    binding.txtLocationArea.setSelected(true)
                    binding.dataLayout.txtTemperature.text =
                        it.data?.current?.temp_c?.toInt().toString()
                    binding.dataLayout.txtDescription.text =
                        it.data?.current?.condition?.text.toString()
                    binding.dataLayout.txtWindSpeed.text =
                        it.data?.current?.wind_kph.toString() + "\nkm/h"
                    binding.dataLayout.txtWindPressure.text =
                        it.data?.current?.pressure_mb.toString() + "\nmb"
                    binding.dataLayout.txtWindHumidity.text =
                        it.data?.current?.humidity.toString() + "%"
                    binding.dataLayout.txtUV.text = it.data?.current?.uv.toString()
                    binding.dataLayout.txtVisibility.text =
                        it.data?.current?.vis_km.toString() + " km"
                    binding.dataLayout.txtFeelsLike.text =
                        it.data?.current?.feelslike_c.toString() + "°C"
                }

                is NetworkResponse.Error -> {
                    binding.simmerLayout.visibility = View.GONE
                    binding.mainLayout.visibility = View.VISIBLE
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                    binding.txtLocationArea.text = null
                    binding.dataLayout.txtTemperature.text = null
                    binding.dataLayout.txtDescription.text = null
                    binding.dataLayout.txtWindSpeed.text = null
                    binding.dataLayout.txtWindPressure.text = null
                    binding.dataLayout.txtWindHumidity.text = null
                    binding.dataLayout.txtUV.text = null
                    binding.dataLayout.txtVisibility.text = null
                    binding.dataLayout.txtFeelsLike.text = null
                }
            }
        }

        homeViewModel.searchWeatherResponse.observe(this as LifecycleOwner) {
            when (it) {
                is NetworkResponse.Loading -> {
                    binding.simmerLayout.visibility = View.VISIBLE
                    binding.mainLayout.visibility = View.GONE
                }

                is NetworkResponse.Success -> {
                    binding.simmerLayout.visibility = View.GONE
                    binding.mainLayout.visibility = View.VISIBLE
                    val query = it.data?.get(0)?.lat.toString() + "," + it.data?.get(0)?.lon
                    updatedLocation = query
                    Log.d("LOC", query)
                    fetchLocation(query)

                    binding.searchView.setQuery("", false)
                    binding.searchView.clearFocus()
                    binding.searchView.visibility = View.GONE
                    binding.txtLocationArea.visibility = View.VISIBLE
                    binding.locationIcon.visibility = View.VISIBLE
                    binding.btnSearch.setIconResource(R.drawable.icon_search)
                    binding.btnRefresh.setIconResource(R.drawable.icon_sync)
                    searchClick = false
                    refreshClick = false
                }

                is NetworkResponse.Error -> {
                    binding.simmerLayout.visibility = View.GONE
                    binding.mainLayout.visibility = View.VISIBLE
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                    binding.searchView.setQuery("", false)
                    binding.searchView.clearFocus()
                    binding.searchView.visibility = View.GONE
                    binding.txtLocationArea.visibility = View.VISIBLE
                    binding.locationIcon.visibility = View.VISIBLE
                    binding.btnSearch.setIconResource(R.drawable.icon_search)
                    binding.btnRefresh.setIconResource(R.drawable.icon_sync)
                    searchClick = false
                    refreshClick = false
                }
            }
        }

        homeViewModel.forecastWeatherResponse.observe(this as LifecycleOwner) {
            when (it) {
                is NetworkResponse.Loading -> {
                    binding.simmerLayout.visibility = View.VISIBLE
                    binding.mainLayout.visibility = View.GONE
                }

                is NetworkResponse.Success -> {
                    binding.simmerLayout.visibility = View.GONE
                    binding.mainLayout.visibility = View.VISIBLE
                    val forecastDay = it.data?.forecast?.forecastday
                    if (forecastDay != null) {
                        binding.dataLayout.todayRecyclerView.adapter = ForecastAdapter(forecastDay[0].hour)
                        binding.dataLayout.tomorrowRecyclerView.adapter = ForecastAdapter(forecastDay[1].hour)
                        binding.dataLayout.dayAfterTomorrowRecyclerView.adapter = ForecastAdapter(forecastDay[2].hour)
                    }else{
                        Toast.makeText(this, "No data found for forecast", Toast.LENGTH_SHORT).show()
                    }
                }

                is NetworkResponse.Error -> {
                    binding.simmerLayout.visibility = View.GONE
                    binding.mainLayout.visibility = View.VISIBLE
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onClick(view: View?) {
        when (view) {
            binding.btnSearch -> {
                if (!searchClick) { // btn search
                    binding.searchView.visibility = View.VISIBLE
                    binding.searchView.requestFocus()
                    binding.txtLocationArea.visibility = View.GONE
                    binding.locationIcon.visibility = View.GONE
                    binding.btnSearch.setIconResource(R.drawable.ic_close)
                    binding.btnRefresh.setIconResource(R.drawable.icon_my_location)
                    searchClick = true
                    refreshClick = true
                } else { // btn cancel search
                    binding.searchView.setQuery("", false)
                    binding.searchView.clearFocus()
                    binding.searchView.visibility = View.GONE
                    binding.txtLocationArea.visibility = View.VISIBLE
                    binding.locationIcon.visibility = View.VISIBLE
                    binding.btnSearch.setIconResource(R.drawable.icon_search)
                    binding.btnRefresh.setIconResource(R.drawable.icon_sync)
                    searchClick = false
                    refreshClick = false
                }
            }

            binding.btnRefresh -> {
                if (!refreshClick) { //btn refresh location
                    fetchLocation(updatedLocation)
                } else { // my current location
                    fetchLocation()
                    binding.searchView.visibility = View.GONE
                    binding.txtLocationArea.visibility = View.VISIBLE
                    binding.locationIcon.visibility = View.VISIBLE
                    binding.btnSearch.setIconResource(R.drawable.icon_search)
                    binding.btnRefresh.setIconResource(R.drawable.icon_sync)
                    searchClick = false
                }
            }
        }
    }

    override fun onRestart() {
        super.onRestart()
        fetchLocation()
    }

    private fun setWeatherIcon(weatherType: String) {
        when (weatherType.trim().lowercase()) {
            "sunny" ->  binding.dataLayout.weatherImage.setImageResource(R.drawable.icon_sunny)
            "clear" ->  binding.dataLayout.weatherImage.setImageResource(R.drawable.icon_weather_cloud_sun)
            "partly cloudy" ->  binding.dataLayout.weatherImage.setImageResource(R.drawable.icon_weather_cloud_sun)
            "mist" -> binding.dataLayout.weatherImage.setImageResource(R.drawable.icon_weather_cloud)
            "overcast" -> binding.dataLayout.weatherImage.setImageResource(R.drawable.icon_weather_cloud)
            "patchy rain nearby" ->binding.dataLayout.weatherImage.setImageResource(R.drawable.icon_weather_rain_cloud)
            "light rain" -> binding.dataLayout.weatherImage.setImageResource(R.drawable.icon_weather_rain_cloud)
            "patchy light rain in area with thunder" -> binding.dataLayout.weatherImage.setImageResource(R.drawable.icon_weather_thunderstorm_cloud)
            "patchy light drizzle" -> binding.dataLayout.weatherImage.setImageResource(R.drawable.icon_weather_thunderstorm_cloud)
            "snow" -> binding.dataLayout.weatherImage.setImageResource(R.drawable.icon_weather_snow_cloud)

            else -> binding.dataLayout.weatherImage.setImageResource(R.drawable.icon_weather_cloud)
        }
    }
}