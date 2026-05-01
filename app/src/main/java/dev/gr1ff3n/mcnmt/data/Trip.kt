package dev.gr1ff3n.mcnmt.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.gr1ff3n.mcnmt.domain.model.TripCategory
import dev.gr1ff3n.mcnmt.domain.model.TripEndSource
import dev.gr1ff3n.mcnmt.domain.model.TripSource
import java.time.Instant

@Entity(
    tableName = "trips",
    foreignKeys = [
        ForeignKey(
            entity = Vehicle::class,
            parentColumns = ["id"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("vehicleId"), Index("startTimeUtc")],
)
data class Trip(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long,
    val startTimeUtc: Instant,
    val endTimeUtc: Instant?,
    val distanceMeters: Double,
    val distanceCorrectionMeters: Double? = null,
    val source: TripSource,
    val endSource: TripEndSource? = null,
    val category: TripCategory = TripCategory.UNREVIEWED,
    val purpose: String? = null,
    val destinationLabel: String? = null,
    val startLatitude: Double? = null,
    val startLongitude: Double? = null,
    val endLatitude: Double? = null,
    val endLongitude: Double? = null,
    val notes: String? = null,
)
