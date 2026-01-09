package com.cosplayjournal.app.data.repository

import com.cosplayjournal.app.data.dao.LocationDao
import com.cosplayjournal.app.data.entity.Location
import kotlinx.coroutines.flow.Flow

class LocationRepository(private val locationDao: LocationDao) {
    val allLocations: Flow<List<Location>> = locationDao.getAllLocations()

    suspend fun insertLocation(location: Location) = locationDao.insertLocation(location)
    suspend fun updateLocation(location: Location) = locationDao.updateLocation(location)
    suspend fun deleteLocation(location: Location) = locationDao.deleteLocation(location)
}
