package dev.gr1ff3n.mcnmt.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Query("SELECT * FROM trips ORDER BY startTimeUtc DESC")
    fun observeAll(): Flow<List<Trip>>

    @Query("SELECT * FROM trips WHERE endTimeUtc IS NULL ORDER BY startTimeUtc DESC LIMIT 1")
    fun observeActive(): Flow<Trip?>

    @Query("SELECT COUNT(*) FROM trips")
    fun observeCount(): Flow<Int>

    @Query("SELECT * FROM trips WHERE id = :id")
    suspend fun getById(id: Long): Trip?

    @Insert
    suspend fun insert(trip: Trip): Long

    @Update
    suspend fun update(trip: Trip)
}
