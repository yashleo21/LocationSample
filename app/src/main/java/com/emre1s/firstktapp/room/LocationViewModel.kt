package com.emre1s.firstktapp.room

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocationViewModel(application: Application): AndroidViewModel(application) {
    private val locationDao: LocationDao
    private val repository: LocationRepository
    val allLocations: LiveData<List<LocationLog>>

    init {
        //Room initialization
        locationDao = LocationRoomDatabase.getDatabase(application).locationDao()
        repository = LocationRepository(locationDao)
        allLocations =  repository.allLocations
    }

    fun insert(locationLog: LocationLog) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(locationLog)
        Log.d("Emre1s", "Insertion successful")
    }

    fun deleteAll() = viewModelScope.launch(Dispatchers.IO) {
        repository.delete()
    }
}