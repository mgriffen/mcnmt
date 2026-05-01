package dev.gr1ff3n.mcnmt.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val isDefault: Boolean = false,
    val createdAt: Instant,
)
