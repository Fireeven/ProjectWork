package com.example.projectwork.repository

import com.example.projectwork.data.PlaceDao
import com.example.projectwork.data.PlaceEntity
import kotlinx.coroutines.flow.Flow

class PlaceRepository(private val placeDao: PlaceDao) {
    fun getAllPlaces(): Flow<List<PlaceEntity>> = placeDao.getAll()

    suspend fun getPlaceById(id: Int): PlaceEntity? = placeDao.getById(id)

    suspend fun upsertPlace(place: PlaceEntity) = placeDao.upsert(place)

    suspend fun deletePlace(place: PlaceEntity) = placeDao.delete(place)
} 