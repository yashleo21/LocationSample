package com.emre1s.firstktapp.room

import androidx.annotation.WorkerThread

class LocationRepository(private val locationDao: LocationDao) {
    val allLocations = locationDao.getAllLocations()

    @WorkerThread
    suspend fun insert(locationLog: LocationLog) {
        locationDao.insert(locationLog)
    }
}