package com.emre1s.firstktapp.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LocationDao {

    @Query("SELECT * from location_table")
    fun getAllLocations(): LiveData<List<LocationLog>>

    @Insert
    suspend fun insert(locationLog: LocationLog)

    @Query("DELETE FROM location_table")
    fun deleteAll()
}