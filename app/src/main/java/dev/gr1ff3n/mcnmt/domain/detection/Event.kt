package dev.gr1ff3n.mcnmt.domain.detection

import java.time.Instant

/** External signals fed into the state machine. */
sealed class Event {
    abstract val timestampUtc: Instant

    /** GPS update arrived from FusedLocationProviderClient. */
    data class LocationUpdate(
        override val timestampUtc: Instant,
        val latitude: Double,
        val longitude: Double,
        val accuracyMeters: Float,
        val speedMps: Float?,
    ) : Event()

    /** Activity Recognition transitioned. */
    data class ActivityRecognized(
        override val timestampUtc: Instant,
        val isInVehicle: Boolean,
        val confidence: Int,
    ) : Event()

    /** User tapped the manual start button. */
    data class ManualStart(override val timestampUtc: Instant) : Event()

    /** User tapped the manual stop button. */
    data class ManualStop(override val timestampUtc: Instant) : Event()

    /** Wall clock advanced — re-evaluate timeouts and the work-hours window. */
    data class Tick(override val timestampUtc: Instant) : Event()

    /** User toggled the auto-detection master switch. */
    data class AutoDetectionToggled(
        override val timestampUtc: Instant,
        val enabled: Boolean,
    ) : Event()
}
