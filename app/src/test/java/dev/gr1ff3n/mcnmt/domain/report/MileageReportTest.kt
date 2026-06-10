package dev.gr1ff3n.mcnmt.domain.report

import dev.gr1ff3n.mcnmt.data.Trip
import dev.gr1ff3n.mcnmt.data.settings.MileageProfile
import dev.gr1ff3n.mcnmt.domain.model.TripSource
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MileageReportTest {

    private val zone: ZoneId = ZoneOffset.UTC
    private val profile = MileageProfile(
        employeeName = "Matt Griffen",
        department = "MCN Broadband",
        accountNumber = "100-4500",
        ratePerMile = 0.725,
        rateLabel = "2026 IRS standard",
    )

    private fun miles(m: Double) = m * 1609.344

    private fun trip(
        id: Long,
        date: String,
        distanceMeters: Double,
        correction: Double? = null,
        destination: String? = null,
        purpose: String? = null,
    ) = Trip(
        id = id,
        vehicleId = 1,
        startTimeUtc = Instant.parse(date),
        endTimeUtc = Instant.parse(date),
        distanceMeters = distanceMeters,
        distanceCorrectionMeters = correction,
        source = TripSource.MANUAL,
        destinationLabel = destination,
        purpose = purpose,
    )

    @Test
    fun `build filters to the period and rounds to whole miles`() {
        val trips = listOf(
            trip(1, "2026-06-03T17:00:00Z", miles(62.4), destination = "Office → Ukiah"),
            trip(2, "2026-06-20T18:00:00Z", miles(10.0), purpose = "Vendor"),
            trip(3, "2026-05-15T17:00:00Z", miles(99.0)), // different month — excluded
        )

        val report = MileageReport.build(trips, YearMonth.of(2026, 6), profile, zone)

        assertEquals(2, report.rows.size)
        assertEquals(62, report.rows[0].miles)          // 62.4 → 62
        assertEquals(72, report.totalMiles)             // 62 + 10
        assertEquals(44.95, report.rows[0].amount, 0.001) // 62 * 0.725
        assertEquals(52.20, report.totalAmount, 0.001)    // 44.95 + 7.25
    }

    @Test
    fun `correction overrides gps distance`() {
        val trips = listOf(
            trip(1, "2026-06-03T17:00:00Z", distanceMeters = miles(80.0), correction = miles(50.0)),
        )
        val report = MileageReport.build(trips, YearMonth.of(2026, 6), profile, zone)
        assertEquals(50, report.rows.single().miles)
    }

    @Test
    fun `location line joins destination and purpose`() {
        val trips = listOf(
            trip(1, "2026-06-03T17:00:00Z", miles(5.0), destination = "Ukiah", purpose = "Meeting"),
        )
        val report = MileageReport.build(trips, YearMonth.of(2026, 6), profile, zone)
        assertEquals("Ukiah — Meeting", report.rows.single().locationAndExplanation)
    }

    @Test
    fun `csv contains header block, rows, and totals with escaping`() {
        val trips = listOf(
            trip(1, "2026-06-03T17:00:00Z", miles(62.0), destination = "Office, Fort Bragg"),
        )
        val report = MileageReport.build(trips, YearMonth.of(2026, 6), profile, zone)
        val csv = CsvBuilder.build(report)

        assertTrue(csv.contains("Employee,Matt Griffen"))
        assertTrue(csv.contains("Period,June 2026"))
        assertTrue(csv.contains("Date,Location of Travel & Explanation,Miles,Amount"))
        assertTrue(csv.contains("\"Office, Fort Bragg\"")) // comma forces quoting
        assertTrue(csv.contains("62,44.95"))
        assertTrue(csv.contains("TOTALS,,62,44.95"))
    }
}
