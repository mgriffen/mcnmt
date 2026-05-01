package dev.gr1ff3n.mcnmt.domain.detection

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object DistanceCalc {
    private const val EARTH_RADIUS_M: Double = 6_371_000.0

    /** Haversine distance between two lat/lon coordinates, in meters. */
    fun distanceMeters(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double,
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val sinHalfDLat = sin(dLat / 2)
        val sinHalfDLon = sin(dLon / 2)
        val a = sinHalfDLat * sinHalfDLat +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sinHalfDLon * sinHalfDLon
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_M * c
    }

    /**
     * Decide whether a new fix should advance the trip's distance counter.
     * Drops fixes with bad accuracy (jitter / first-fix garbage) and
     * sub-noise deltas (parked-engine wobble).
     */
    fun shouldAcceptForDistance(
        prevLat: Double, prevLon: Double,
        newLat: Double, newLon: Double,
        newAccuracyMeters: Float,
        accuracyMaxMeters: Double = DetectionConstants.ACCURACY_FILTER_METERS,
        minStepMeters: Double = DetectionConstants.MIN_STEP_METERS,
    ): Acceptance {
        if (newAccuracyMeters > accuracyMaxMeters) return Acceptance.RejectedAccuracy
        val delta = distanceMeters(prevLat, prevLon, newLat, newLon)
        if (delta < minStepMeters) return Acceptance.RejectedTooSmall(delta)
        return Acceptance.Accepted(delta)
    }

    sealed class Acceptance {
        data class Accepted(val deltaMeters: Double) : Acceptance()
        data object RejectedAccuracy : Acceptance()
        data class RejectedTooSmall(val deltaMeters: Double) : Acceptance()
    }
}
