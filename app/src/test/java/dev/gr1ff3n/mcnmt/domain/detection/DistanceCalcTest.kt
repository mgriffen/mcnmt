package dev.gr1ff3n.mcnmt.domain.detection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DistanceCalcTest {
    @Test
    fun `same point distance is zero`() {
        val d = DistanceCalc.distanceMeters(37.0, -122.0, 37.0, -122.0)
        assertEquals(0.0, d, 0.001)
    }

    @Test
    fun `one degree latitude is roughly 111 km`() {
        val d = DistanceCalc.distanceMeters(37.0, -122.0, 38.0, -122.0)
        // 1° latitude ≈ 111.195 km — accept ±200 m
        assertEquals(111_195.0, d, 200.0)
    }

    @Test
    fun `accepts a 25m delta with 5m accuracy`() {
        val r = DistanceCalc.shouldAcceptForDistance(
            prevLat = 37.0, prevLon = -122.0,
            // 25m east at this latitude is ~0.000284° lon
            newLat = 37.0, newLon = -122.0 + 0.000284,
            newAccuracyMeters = 5f,
        )
        assertTrue("expected Accepted but was $r", r is DistanceCalc.Acceptance.Accepted)
        val accepted = r as DistanceCalc.Acceptance.Accepted
        assertEquals(25.0, accepted.deltaMeters, 1.0)
    }

    @Test
    fun `rejects bad-accuracy fix`() {
        val r = DistanceCalc.shouldAcceptForDistance(
            prevLat = 37.0, prevLon = -122.0,
            newLat = 37.001, newLon = -122.0,
            newAccuracyMeters = 50f,
        )
        assertTrue(r is DistanceCalc.Acceptance.RejectedAccuracy)
    }

    @Test
    fun `rejects tiny delta below min step`() {
        val r = DistanceCalc.shouldAcceptForDistance(
            prevLat = 37.0, prevLon = -122.0,
            // 5m east is well under the 10m min step
            newLat = 37.0, newLon = -122.0 + 0.0000568,
            newAccuracyMeters = 5f,
        )
        assertTrue("expected RejectedTooSmall but was $r", r is DistanceCalc.Acceptance.RejectedTooSmall)
    }
}
