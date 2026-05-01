package dev.gr1ff3n.mcnmt.data.location

import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.android.asCoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import android.os.Looper

@Singleton
class LocationProvider @Inject constructor(
    private val client: FusedLocationProviderClient,
) {
    /**
     * Cold flow of [Location] updates. Subscribes to FusedLocationProviderClient
     * on first collection; unsubscribes when the collector cancels.
     *
     * Caller is responsible for ensuring `ACCESS_FINE_LOCATION` is granted
     * before collecting.
     */
    @SuppressLint("MissingPermission")
    fun observeLocations(): Flow<Location> = callbackFlow {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, INTERVAL_TARGET_MS)
            .setMinUpdateIntervalMillis(INTERVAL_MIN_MS)
            .setMaxUpdateDelayMillis(INTERVAL_MAX_MS)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.forEach { trySend(it) }
            }
        }

        client.requestLocationUpdates(request, callback, Looper.getMainLooper())

        awaitClose { client.removeLocationUpdates(callback) }
    }

    private companion object {
        const val INTERVAL_TARGET_MS = 3_000L
        const val INTERVAL_MIN_MS = 2_000L
        const val INTERVAL_MAX_MS = 10_000L
    }
}
