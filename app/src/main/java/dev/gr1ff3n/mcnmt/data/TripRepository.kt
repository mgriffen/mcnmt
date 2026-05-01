package dev.gr1ff3n.mcnmt.data

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class TripRepository @Inject constructor(
    private val vehicleDao: VehicleDao,
    private val tripDao: TripDao,
    private val tripPointDao: TripPointDao,
) {
    fun observeTrips(): Flow<List<Trip>> = tripDao.observeAll()
    fun observeActiveTrip(): Flow<Trip?> = tripDao.observeActive()
    fun observeTripCount(): Flow<Int> = tripDao.observeCount()

    suspend fun getDefaultVehicle(): Vehicle? = vehicleDao.getDefault()
    suspend fun pointCountForTrip(tripId: Long): Int = tripPointDao.countForTrip(tripId)
}
