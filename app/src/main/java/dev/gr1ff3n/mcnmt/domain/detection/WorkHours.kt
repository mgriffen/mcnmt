package dev.gr1ff3n.mcnmt.domain.detection

import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId

object WorkHours {
    private val WORK_DAYS = setOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
    )

    /**
     * True iff [instant] interpreted in [zone] falls within work hours
     * (default 09:00..<18:00 Mon–Fri). End hour is exclusive — at exactly
     * 18:00 the window closes.
     */
    fun isWithinWorkHours(
        instant: Instant,
        zone: ZoneId = ZoneId.systemDefault(),
        startHour: Int = DetectionConstants.WORK_HOUR_START,
        endHour: Int = DetectionConstants.WORK_HOUR_END,
    ): Boolean {
        val zdt = instant.atZone(zone)
        if (zdt.dayOfWeek !in WORK_DAYS) return false
        return zdt.hour in startHour until endHour
    }
}
