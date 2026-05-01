package dev.gr1ff3n.mcnmt.domain.detection

import dev.gr1ff3n.mcnmt.domain.model.TripSource
import java.time.Instant

/**
 * Detection state. Pure data — the [TripStateMachine] computes transitions
 * and the [TripTrackingService] persists the side effects.
 *
 * The state machine does NOT track the database `tripId` of the active
 * trip — the service owns that. Keeping the state machine free of IDs
 * keeps it pure-testable.
 */
sealed class State {
    /** No active trip, no GPS request. AR listener may still be registered. */
    data object Idle : State()

    /**
     * AR fired IN_VEHICLE during work hours. GPS is on, points are
     * arriving, but no Trip row exists yet. Waiting for the
     * 10-mph-for-30s confirmation.
     */
    data class Arming(
        val startedAtUtc: Instant,
        val firstPoint: PointSummary?,
        val sustainedAboveSpeedSinceUtc: Instant?,
    ) : State()

    /** Trip is officially in progress. */
    data class Tracking(
        val source: TripSource,
        val startedAtUtc: Instant,
        val lastPoint: PointSummary?,
        /** First time we saw sustained-low-speed; reset to null when speed climbs. */
        val sustainedBelowSpeedSinceUtc: Instant?,
    ) : State()
}

data class PointSummary(
    val timestampUtc: Instant,
    val latitude: Double,
    val longitude: Double,
    val speedMps: Float?,
)
