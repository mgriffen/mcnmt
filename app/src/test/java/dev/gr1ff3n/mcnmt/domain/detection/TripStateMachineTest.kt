package dev.gr1ff3n.mcnmt.domain.detection

import dev.gr1ff3n.mcnmt.domain.model.TripEndSource
import dev.gr1ff3n.mcnmt.domain.model.TripSource
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TripStateMachineTest {
    private val pacific = ZoneId.of("America/Los_Angeles")

    /** A Wednesday at 12:30pm Pacific — squarely inside work hours. */
    private val workHoursNoon: Instant =
        LocalDateTime.of(2026, 4, 29, 12, 30).atZone(pacific).toInstant()

    /** A Saturday at 12:30pm Pacific — outside work hours. */
    private val saturdayNoon: Instant =
        LocalDateTime.of(2026, 5, 2, 12, 30).atZone(pacific).toInstant()

    private fun loc(
        t: Instant,
        speedMps: Float? = 0f,
        lat: Double = 37.0,
        lon: Double = -122.0,
        accuracy: Float = 5f,
    ) = Event.LocationUpdate(t, lat, lon, accuracy, speedMps)

    // ───────── IDLE ─────────

    @Test
    fun `idle plus manual start goes to manual tracking`() {
        val r = TripStateMachine.step(State.Idle, Event.ManualStart(workHoursNoon), zone = pacific)
        assertTrue(r.newState is State.Tracking)
        assertEquals(TripSource.MANUAL, (r.newState as State.Tracking).source)
        assertTrue(r.commands.any { it is Command.StartGps })
        assertTrue(r.commands.any { it is Command.CreateTrip })
    }

    @Test
    fun `idle plus high-confidence in-vehicle in work hours arms`() {
        val r = TripStateMachine.step(
            State.Idle,
            Event.ActivityRecognized(workHoursNoon, isInVehicle = true, confidence = 90),
            zone = pacific,
        )
        assertTrue(r.newState is State.Arming)
        assertTrue(r.commands.any { it is Command.StartGps })
    }

    @Test
    fun `idle ignores low-confidence AR`() {
        val r = TripStateMachine.step(
            State.Idle,
            Event.ActivityRecognized(workHoursNoon, isInVehicle = true, confidence = 50),
            zone = pacific,
        )
        assertEquals(State.Idle, r.newState)
        assertTrue(r.commands.isEmpty())
    }

    @Test
    fun `idle ignores AR outside work hours`() {
        val r = TripStateMachine.step(
            State.Idle,
            Event.ActivityRecognized(saturdayNoon, isInVehicle = true, confidence = 95),
            zone = pacific,
        )
        assertEquals(State.Idle, r.newState)
    }

    @Test
    fun `idle ignores AR when auto detection is disabled`() {
        val r = TripStateMachine.step(
            State.Idle,
            Event.ActivityRecognized(workHoursNoon, isInVehicle = true, confidence = 95),
            autoDetectionEnabled = false,
            zone = pacific,
        )
        assertEquals(State.Idle, r.newState)
    }

    @Test
    fun `manual start works outside work hours`() {
        val r = TripStateMachine.step(State.Idle, Event.ManualStart(saturdayNoon), zone = pacific)
        assertTrue(r.newState is State.Tracking)
    }

    // ───────── ARMING ─────────

    @Test
    fun `arming sustains over-speed for 30s and confirms to AUTO tracking`() {
        var s: State = State.Arming(
            startedAtUtc = workHoursNoon,
            firstPoint = null,
            sustainedAboveSpeedSinceUtc = null,
        )
        // First fast point at t=0
        val t0 = workHoursNoon
        s = TripStateMachine.step(s, loc(t0, speedMps = 6f), zone = pacific).newState
        assertTrue(s is State.Arming)

        // Same fast speed 31s later → confirmation
        val t1 = t0.plusSeconds(31)
        val r = TripStateMachine.step(s, loc(t1, speedMps = 6f, lat = 37.001), zone = pacific)
        assertTrue("expected Tracking but was ${r.newState}", r.newState is State.Tracking)
        assertEquals(TripSource.AUTO, (r.newState as State.Tracking).source)
        assertTrue(r.commands.any { it is Command.CreateTrip && it.source == TripSource.AUTO })
    }

    @Test
    fun `arming below-threshold speed does not start the timer`() {
        val s = State.Arming(workHoursNoon, firstPoint = null, sustainedAboveSpeedSinceUtc = null)
        val r = TripStateMachine.step(s, loc(workHoursNoon, speedMps = 3f), zone = pacific)
        assertTrue(r.newState is State.Arming)
        assertNull((r.newState as State.Arming).sustainedAboveSpeedSinceUtc)
    }

    @Test
    fun `arming dropping back below speed resets the timer`() {
        var s: State = State.Arming(workHoursNoon, firstPoint = null, sustainedAboveSpeedSinceUtc = null)
        s = TripStateMachine.step(s, loc(workHoursNoon, speedMps = 6f), zone = pacific).newState
        assertTrue((s as State.Arming).sustainedAboveSpeedSinceUtc != null)

        // 10s later — slow
        val r = TripStateMachine.step(s, loc(workHoursNoon.plusSeconds(10), speedMps = 1f), zone = pacific)
        assertTrue(r.newState is State.Arming)
        assertNull((r.newState as State.Arming).sustainedAboveSpeedSinceUtc)
    }

    @Test
    fun `arming times out after 5 minutes`() {
        val s = State.Arming(workHoursNoon, firstPoint = null, sustainedAboveSpeedSinceUtc = null)
        val r = TripStateMachine.step(
            s,
            Event.Tick(workHoursNoon.plusSeconds(5 * 60 + 1)),
            zone = pacific,
        )
        assertEquals(State.Idle, r.newState)
        assertTrue(r.commands.any { it is Command.DiscardArmingBuffer })
        assertTrue(r.commands.any { it is Command.StopGps })
    }

    @Test
    fun `arming aborts when AR confidently flips to not in vehicle`() {
        val s = State.Arming(workHoursNoon, firstPoint = null, sustainedAboveSpeedSinceUtc = null)
        val r = TripStateMachine.step(
            s,
            Event.ActivityRecognized(workHoursNoon, isInVehicle = false, confidence = 90),
            zone = pacific,
        )
        assertEquals(State.Idle, r.newState)
    }

    @Test
    fun `arming plus manual start escalates to MANUAL tracking and discards buffer`() {
        val s = State.Arming(workHoursNoon, firstPoint = null, sustainedAboveSpeedSinceUtc = null)
        val r = TripStateMachine.step(s, Event.ManualStart(workHoursNoon.plusSeconds(60)), zone = pacific)
        assertTrue(r.newState is State.Tracking)
        assertEquals(TripSource.MANUAL, (r.newState as State.Tracking).source)
        assertTrue(r.commands.any { it is Command.DiscardArmingBuffer })
    }

    // ───────── TRACKING ─────────

    @Test
    fun `tracking plus manual stop ends the trip`() {
        val s = State.Tracking(
            source = TripSource.MANUAL,
            startedAtUtc = workHoursNoon,
            lastPoint = null,
            sustainedBelowSpeedSinceUtc = null,
        )
        val r = TripStateMachine.step(s, Event.ManualStop(workHoursNoon.plusSeconds(10)), zone = pacific)
        assertEquals(State.Idle, r.newState)
        val end = r.commands.filterIsInstance<Command.EndTrip>().single()
        assertEquals(TripEndSource.MANUAL_STOP, end.endSource)
    }

    @Test
    fun `tracking accumulates a point when delta is large enough`() {
        val s = State.Tracking(
            source = TripSource.MANUAL,
            startedAtUtc = workHoursNoon,
            lastPoint = PointSummary(workHoursNoon, 37.0, -122.0, 5f),
            sustainedBelowSpeedSinceUtc = null,
        )
        // ~100m east
        val r = TripStateMachine.step(
            s,
            loc(workHoursNoon.plusSeconds(5), speedMps = 5f, lat = 37.0, lon = -122.0 + 0.001137),
            zone = pacific,
        )
        assertTrue(r.newState is State.Tracking)
        val add = r.commands.filterIsInstance<Command.AddPoint>().single()
        assertTrue("expected ~100m delta, got ${add.deltaMeters}", add.deltaMeters in 95.0..105.0)
    }

    @Test
    fun `tracking ignores tiny delta below min step`() {
        val s = State.Tracking(
            source = TripSource.MANUAL,
            startedAtUtc = workHoursNoon,
            lastPoint = PointSummary(workHoursNoon, 37.0, -122.0, 5f),
            sustainedBelowSpeedSinceUtc = null,
        )
        // ~5m east — below 10m min step
        val r = TripStateMachine.step(
            s,
            loc(workHoursNoon.plusSeconds(2), speedMps = 0f, lat = 37.0, lon = -122.0 + 0.0000568),
            zone = pacific,
        )
        assertTrue(r.commands.filterIsInstance<Command.AddPoint>().isEmpty())
    }

    @Test
    fun `manual tracking does not auto-end on slow speed`() {
        val s = State.Tracking(
            source = TripSource.MANUAL,
            startedAtUtc = workHoursNoon,
            lastPoint = PointSummary(workHoursNoon, 37.0, -122.0, 0f),
            sustainedBelowSpeedSinceUtc = workHoursNoon, // pretend we've been slow for ages
        )
        val r = TripStateMachine.step(
            s,
            loc(workHoursNoon.plusSeconds(11 * 60), speedMps = 0f),
            zone = pacific,
        )
        // Still tracking — manual trips only end on ManualStop.
        assertTrue(r.newState is State.Tracking)
        assertTrue(r.commands.filterIsInstance<Command.EndTrip>().isEmpty())
    }

    @Test
    fun `auto tracking auto-ends after 10 minutes of slow speed`() {
        val below = workHoursNoon
        val s = State.Tracking(
            source = TripSource.AUTO,
            startedAtUtc = workHoursNoon.minusSeconds(60),
            lastPoint = PointSummary(below, 37.0, -122.0, 0f),
            sustainedBelowSpeedSinceUtc = below,
        )
        val r = TripStateMachine.step(
            s,
            loc(below.plusSeconds(10 * 60 + 1), speedMps = 0.1f, lat = 37.000001),
            zone = pacific,
        )
        assertEquals(State.Idle, r.newState)
        val end = r.commands.filterIsInstance<Command.EndTrip>().single()
        assertEquals(TripEndSource.AUTO_TIMEOUT, end.endSource)
    }

    @Test
    fun `auto tracking ends when work hours close`() {
        val s = State.Tracking(
            source = TripSource.AUTO,
            startedAtUtc = workHoursNoon,
            lastPoint = PointSummary(workHoursNoon, 37.0, -122.0, 5f),
            sustainedBelowSpeedSinceUtc = null,
        )
        // 6:01 PM Pacific — outside work hours
        val afterHours = LocalDateTime.of(2026, 4, 29, 18, 1).atZone(pacific).toInstant()
        val r = TripStateMachine.step(s, Event.Tick(afterHours), zone = pacific)
        assertEquals(State.Idle, r.newState)
        val end = r.commands.filterIsInstance<Command.EndTrip>().single()
        assertEquals(TripEndSource.WORK_HOURS_ENDED, end.endSource)
    }

    @Test
    fun `manual tracking does not auto-end at work hours close`() {
        val s = State.Tracking(
            source = TripSource.MANUAL,
            startedAtUtc = workHoursNoon,
            lastPoint = PointSummary(workHoursNoon, 37.0, -122.0, 5f),
            sustainedBelowSpeedSinceUtc = null,
        )
        val afterHours = LocalDateTime.of(2026, 4, 29, 19, 0).atZone(pacific).toInstant()
        val r = TripStateMachine.step(s, Event.Tick(afterHours), zone = pacific)
        assertTrue(r.newState is State.Tracking)
    }

    @Test
    fun `tracking ignores manual start (already tracking)`() {
        val s = State.Tracking(
            source = TripSource.AUTO,
            startedAtUtc = workHoursNoon,
            lastPoint = null,
            sustainedBelowSpeedSinceUtc = null,
        )
        val r = TripStateMachine.step(s, Event.ManualStart(workHoursNoon.plusSeconds(60)), zone = pacific)
        assertTrue(r.newState is State.Tracking)
        assertTrue(r.commands.isEmpty())
    }
}
