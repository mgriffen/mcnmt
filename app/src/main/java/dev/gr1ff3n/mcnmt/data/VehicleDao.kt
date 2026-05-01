package dev.gr1ff3n.mcnmt.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefault(): Vehicle?

    @Query("SELECT * FROM vehicles ORDER BY id")
    fun observeAll(): Flow<List<Vehicle>>

    @Query("SELECT COUNT(*) FROM vehicles")
    suspend fun count(): Int

    @Insert
    suspend fun insert(vehicle: Vehicle): Long

    @Update
    suspend fun update(vehicle: Vehicle)
}
