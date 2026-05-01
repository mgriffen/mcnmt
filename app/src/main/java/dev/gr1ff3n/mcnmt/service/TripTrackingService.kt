package dev.gr1ff3n.mcnmt.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import dagger.hilt.android.AndroidEntryPoint
import dev.gr1ff3n.mcnmt.MainActivity
import dev.gr1ff3n.mcnmt.R
import dev.gr1ff3n.mcnmt.data.Trip
import dev.gr1ff3n.mcnmt.data.TripPoint
import dev.gr1ff3n.mcnmt.data.TripRepository
import dev.gr1ff3n.mcnmt.data.location.LocationProvider
import dev.gr1ff3n.mcnmt.domain.detection.Command
import dev.gr1ff3n.mcnmt.domain.detection.Event
import dev.gr1ff3n.mcnmt.domain.detection.PointSummary
import dev.gr1ff3n.mcnmt.domain.detection.State
import dev.gr1ff3n.mcnmt.domain.detection.TripStateMachine
import dev.gr1ff3n.mcnmt.domain.model.TripCategory
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Foreground service that owns the active trip's lifecycle.
 *
 * Receives [Event]s (Manual start/stop, GPS updates), runs them through
 * [TripStateMachine], and persists the resulting [Command]s via
 * [TripRepository]. Slice 4 handles manual start/stop; Slice 5 will add
 * Activity Recognition wiring through this same channel.
 */
@AndroidEntryPoint
class TripTrackingService : Service() {

    @Inject lateinit var locationProvider: LocationProvider
    @Inject lateinit var repository: TripRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val events = MutableSharedFlow<Event>(extraBufferCapacity = 64)
    private val mutex = Mutex()

    // Service-owned canonical state. Mutated only inside `mutex`.
    private var state: State = State.Idle
    private var currentTripId: Long? = null
    private var locationJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        ensureNotificationChannel()

        scope.launch {
            events.collect { event ->
                mutex.withLock { handleEvent(event) }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Android 14+ requires startForeground within ~5s, with the matching
        // foregroundServiceType + permission. Promote unconditionally so we
        // satisfy that contract before any async work.
        promoteToForeground(text = getString(R.string.notif_tracking_text))

        when (intent?.action) {
            ACTION_MANUAL_START -> events.tryEmit(Event.ManualStart(Instant.now()))
            ACTION_MANUAL_STOP -> events.tryEmit(Event.ManualStop(Instant.now()))
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    // ───────────── Event / Command pipeline ─────────────

    private suspend fun handleEvent(event: Event) {
        val (newState, commands) = TripStateMachine.step(state, event)
        state = newState
        commands.forEach { execute(it) }
        updateForegroundForState()
        if (newState is State.Idle && currentTripId == null) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private suspend fun execute(command: Command) {
        when (command) {
            Command.StartGps -> startGps()
            Command.StopGps -> stopGps()
            Command.DiscardArmingBuffer -> Unit  // Slice 5 — no buffer yet

            is Command.CreateTrip -> createTrip(command)
            is Command.AddPoint -> addPoint(command)
            is Command.EndTrip -> endTrip(command)
        }
    }

    private suspend fun createTrip(cmd: Command.CreateTrip) {
        val vehicle = repository.getDefaultVehicle() ?: return
        val tripId = repository.insertTrip(
            Trip(
                vehicleId = vehicle.id,
                startTimeUtc = cmd.startTimeUtc,
                endTimeUtc = null,
                distanceMeters = 0.0,
                source = cmd.source,
                category = TripCategory.UNREVIEWED,
                startLatitude = cmd.firstPoint?.latitude,
                startLongitude = cmd.firstPoint?.longitude,
            )
        )
        currentTripId = tripId
    }

    private suspend fun addPoint(cmd: Command.AddPoint) {
        val tripId = currentTripId ?: return
        repository.addPoint(
            TripPoint(
                tripId = tripId,
                timestampUtc = cmd.point.timestampUtc,
                latitude = cmd.point.latitude,
                longitude = cmd.point.longitude,
                accuracyMeters = cmd.accuracyMeters,
                speedMps = cmd.point.speedMps,
            )
        )
        if (cmd.deltaMeters > 0.0) {
            val trip = repository.getTrip(tripId) ?: return
            repository.updateTrip(trip.copy(distanceMeters = trip.distanceMeters + cmd.deltaMeters))
        }
    }

    private suspend fun endTrip(cmd: Command.EndTrip) {
        val tripId = currentTripId ?: return
        val trip = repository.getTrip(tripId) ?: return
        repository.updateTrip(
            trip.copy(
                endTimeUtc = cmd.endTimeUtc,
                endSource = cmd.endSource,
                endLatitude = cmd.endPoint?.latitude,
                endLongitude = cmd.endPoint?.longitude,
            )
        )
        currentTripId = null
    }

    // ───────────── GPS lifecycle ─────────────

    private fun startGps() {
        if (locationJob?.isActive == true) return
        locationJob = scope.launch {
            locationProvider.observeLocations().collect { loc ->
                events.tryEmit(
                    Event.LocationUpdate(
                        timestampUtc = if (loc.time > 0) Instant.ofEpochMilli(loc.time) else Instant.now(),
                        latitude = loc.latitude,
                        longitude = loc.longitude,
                        accuracyMeters = loc.accuracy,
                        speedMps = if (loc.hasSpeed()) loc.speed else null,
                    )
                )
            }
        }
    }

    private fun stopGps() {
        locationJob?.cancel()
        locationJob = null
    }

    // ───────────── Foreground notification ─────────────

    private fun promoteToForeground(text: String) {
        startForeground(
            NOTIF_ID,
            buildNotification(text),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION,
        )
    }

    private suspend fun updateForegroundForState() {
        val text = when (val s = state) {
            is State.Idle -> getString(R.string.notif_tracking_text)
            is State.Arming -> getString(R.string.notif_tracking_title)
            is State.Tracking -> formatTrackingText(s)
        }
        getSystemService<NotificationManager>()?.notify(NOTIF_ID, buildNotification(text))
    }

    private suspend fun formatTrackingText(s: State.Tracking): String {
        val tripId = currentTripId
        val miles = if (tripId == null) 0.0 else {
            (repository.getTrip(tripId)?.distanceMeters ?: 0.0) / METERS_PER_MILE
        }
        return "Tracking · %.2f mi".format(miles)
    }

    private fun buildNotification(text: String): Notification {
        val openIntent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notif_tracking_title))
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(pi)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun ensureNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notif_channel_tracking),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = getString(R.string.notif_channel_tracking_desc)
            setShowBadge(false)
        }
        getSystemService<NotificationManager>()?.createNotificationChannel(channel)
    }

    companion object {
        const val ACTION_MANUAL_START = "dev.gr1ff3n.mcnmt.MANUAL_START"
        const val ACTION_MANUAL_STOP = "dev.gr1ff3n.mcnmt.MANUAL_STOP"

        private const val CHANNEL_ID = "trip_tracking"
        private const val NOTIF_ID = 1001
        private const val METERS_PER_MILE = 1609.344
    }
}
