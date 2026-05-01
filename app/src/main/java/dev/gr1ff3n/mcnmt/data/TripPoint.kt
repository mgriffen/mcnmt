package dev.gr1ff3n.mcnmt.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "trip_points",
    foreignKeys = [
        ForeignKey(
            entity = Trip::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("tripId")],
)
data class TripPoint(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tripId: Long,
    val timestampUtc: Instant,
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val speedMps: Float?,
)
