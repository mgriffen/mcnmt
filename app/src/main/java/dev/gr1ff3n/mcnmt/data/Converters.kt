package dev.gr1ff3n.mcnmt.data

import androidx.room.TypeConverter
import dev.gr1ff3n.mcnmt.domain.model.TripCategory
import dev.gr1ff3n.mcnmt.domain.model.TripEndSource
import dev.gr1ff3n.mcnmt.domain.model.TripSource
import java.time.Instant

class Converters {
    @TypeConverter
    fun instantToMillis(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    fun millisToInstant(value: Long?): Instant? =
        if (value == null) null else Instant.ofEpochMilli(value)

    @TypeConverter
    fun tripSourceToString(value: TripSource?): String? = value?.name

    @TypeConverter
    fun stringToTripSource(value: String?): TripSource? =
        if (value == null) null else TripSource.valueOf(value)

    @TypeConverter
    fun tripEndSourceToString(value: TripEndSource?): String? = value?.name

    @TypeConverter
    fun stringToTripEndSource(value: String?): TripEndSource? =
        if (value == null) null else TripEndSource.valueOf(value)

    @TypeConverter
    fun tripCategoryToString(value: TripCategory?): String? = value?.name

    @TypeConverter
    fun stringToTripCategory(value: String?): TripCategory? =
        if (value == null) null else TripCategory.valueOf(value)
}
