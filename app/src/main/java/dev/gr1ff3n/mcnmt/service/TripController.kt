package dev.gr1ff3n.mcnmt.service

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin facade over the foreground service Intent dance, so ViewModels and
 * other callers don't have to know about the service class directly.
 */
@Singleton
class TripController @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun startManual() {
        val intent = Intent(context, TripTrackingService::class.java).apply {
            action = TripTrackingService.ACTION_MANUAL_START
        }
        context.startForegroundService(intent)
    }

    fun stopManual() {
        val intent = Intent(context, TripTrackingService::class.java).apply {
            action = TripTrackingService.ACTION_MANUAL_STOP
        }
        context.startForegroundService(intent)
    }
}
