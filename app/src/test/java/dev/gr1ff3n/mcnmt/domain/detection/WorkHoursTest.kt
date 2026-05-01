package dev.gr1ff3n.mcnmt.domain.detection

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkHoursTest {
    private val pacific = ZoneId.of("America/Los_Angeles")

    private fun ldt(year: Int, month: Int, day: Int, hour: Int, minute: Int = 0) =
        LocalDateTime.of(year, month, day, hour, minute).atZone(pacific).toInstant()

    @Test
    fun `wednesday noon is in window`() {
        // 2026-04-29 is a Wednesday
        assertTrue(WorkHours.isWithinWorkHours(ldt(2026, 4, 29, 12), pacific))
    }

    @Test
    fun `friday 9am is in window`() {
        // 2026-05-01 is a Friday
        assertTrue(WorkHours.isWithinWorkHours(ldt(2026, 5, 1, 9, 0), pacific))
    }

    @Test
    fun `friday 859am is out of window`() {
        assertFalse(WorkHours.isWithinWorkHours(ldt(2026, 5, 1, 8, 59), pacific))
    }

    @Test
    fun `friday 6pm exactly is out of window`() {
        assertFalse(WorkHours.isWithinWorkHours(ldt(2026, 5, 1, 18, 0), pacific))
    }

    @Test
    fun `friday 559pm is in window`() {
        assertTrue(WorkHours.isWithinWorkHours(ldt(2026, 5, 1, 17, 59), pacific))
    }

    @Test
    fun `saturday is out of window`() {
        // 2026-05-02 is a Saturday
        assertFalse(WorkHours.isWithinWorkHours(ldt(2026, 5, 2, 12), pacific))
    }

    @Test
    fun `sunday is out of window`() {
        // 2026-05-03 is a Sunday
        assertFalse(WorkHours.isWithinWorkHours(ldt(2026, 5, 3, 12), pacific))
    }

    @Test
    fun `monday 5pm is in window`() {
        // 2026-04-27 is a Monday
        assertTrue(WorkHours.isWithinWorkHours(ldt(2026, 4, 27, 17, 30), pacific))
    }
}
