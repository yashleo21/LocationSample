package com.emre1s.firstktapp.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull

@Entity(tableName = "location_table")
data class LocationLog(@PrimaryKey
                       @NotNull
                       val latitude: String,
                       val longitude: String,
                       val address: String)