package dev.gr1ff3n.mcnmt.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Vehicle::class, Trip::class, TripPoint::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class MileageDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun tripDao(): TripDao
    abstract fun tripPointDao(): TripPointDao

    companion object {
        const val NAME = "mileage.db"

        // Inserts the default vehicle on first DB creation. Raw SQL avoids the
        // Hilt-Provider-from-callback dance — we don't have DAOs at this point
        // because the DB is still being constructed.
        val BootstrapCallback: Callback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                db.execSQL(
                    "INSERT INTO vehicles (label, isDefault, createdAt) VALUES (?, ?, ?)",
                    arrayOf<Any>("Primary Vehicle", 1, System.currentTimeMillis()),
                )
            }
        }
    }
}
