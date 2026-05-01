package dev.gr1ff3n.mcnmt.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TripPointDao {
    @Query("SELECT * FROM trip_points WHERE tripId = :tripId ORDER BY timestampUtc")
    fun observeByTrip(tripId: Long): Flow<List<TripPoint>>

    @Query("SELECT COUNT(*) FROM trip_points WHERE tripId = :tripId")
    suspend fun countForTrip(tripId: Long): Int

    @Insert
    suspend fun insert(point: TripPoint): Long

    @Insert
    suspend fun insertAll(points: List<TripPoint>)
}
