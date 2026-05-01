package dev.gr1ff3n.mcnmt.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.gr1ff3n.mcnmt.data.MileageDatabase
import dev.gr1ff3n.mcnmt.data.TripDao
import dev.gr1ff3n.mcnmt.data.TripPointDao
import dev.gr1ff3n.mcnmt.data.VehicleDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MileageDatabase =
        Room.databaseBuilder(context, MileageDatabase::class.java, MileageDatabase.NAME)
            .addCallback(MileageDatabase.BootstrapCallback)
            .build()

    @Provides fun provideVehicleDao(db: MileageDatabase): VehicleDao = db.vehicleDao()
    @Provides fun provideTripDao(db: MileageDatabase): TripDao = db.tripDao()
    @Provides fun provideTripPointDao(db: MileageDatabase): TripPointDao = db.tripPointDao()
}
