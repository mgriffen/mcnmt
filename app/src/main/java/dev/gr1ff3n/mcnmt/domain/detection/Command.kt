package dev.gr1ff3n.mcnmt.domain.detection

import dev.gr1ff3n.mcnmt.domain.model.TripEndSource
import dev.gr1ff3n.mcnmt.domain.model.TripSource
import java.time.Instant

/**
 * Side-effect commands the state machine emits. The service interprets
 * them: starts/stops GPS, writes rows to Room, posts notifications.
 */
sealed class Command {
    /** Begin requesting location updates and put the service in foreground. */
    data object StartGps : Command()

    /** Stop location updates and remove the service from foreground. */
    data object StopGps : Command()

    /** Insert a new Trip row. The service tracks the returned id. */
    data class CreateTrip(
        val source: TripSource,
        val startTimeUtc: Instant,
        val firstPoint: PointSummary?,
    ) : Command()

    /** Append a point to the current trip and increment its distanceMeters. */
    data class AddPoint(
        val point: PointSummary,
        val accuracyMeters: Float,
        val deltaMeters: Double,
    ) : Command()

    /** Finalize the current trip: set endTimeUtc, endSource, end coords. */
    data class EndTrip(
        val endTimeUtc: Instant,
        val endSource: TripEndSource,
        val endPoint: PointSummary?,
    ) : Command()

    /** ARMING failed to confirm — drop any in-memory buffer. */
    data object DiscardArmingBuffer : Command()
}
