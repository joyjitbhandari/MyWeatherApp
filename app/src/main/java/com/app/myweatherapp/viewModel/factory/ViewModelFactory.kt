package com.app.myweatherapp.viewModel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.app.myweatherapp.repository.Repository
import com.app.myweatherapp.viewModel.HomeViewModel

class ViewModelFactory(private val repository: Repository) : ViewModelProvider.NewInstanceFactory() {
    override fun <T:ViewModel> create(modelClass:Class<T>):T{
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(repository) as T

            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }
}