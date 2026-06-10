package dev.gr1ff3n.mcnmt.domain.report

import dev.gr1ff3n.mcnmt.data.Trip
import dev.gr1ff3n.mcnmt.data.settings.MileageProfile
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/** One line on the voucher: a single trip's date, description, miles, and dollar amount. */
data class ReportRow(
    val date: LocalDate,
    val locationAndExplanation: String,
    val miles: Int,
    val amount: Double,
)

/**
 * A month's worth of reimbursable mileage, pre-computed for output (CSV/PDF).
 *
 * Pure and Android-free so it can be unit-tested on the JVM. Miles are rounded
 * to whole numbers (City voucher convention) and the dollar amount is computed
 * from the *rounded* miles × rate, matching how the form's `miles × rate`
 * formula reads on paper.
 */
data class MileageReport(
    val period: YearMonth,
    val profile: MileageProfile,
    val rows: List<ReportRow>,
) {
    val totalMiles: Int = rows.sumOf { it.miles }
    val totalAmount: Double = rows.sumOf { it.amount }

    companion object {
        private const val METERS_PER_MILE = 1609.344

        fun build(
            trips: List<Trip>,
            period: YearMonth,
            profile: MileageProfile,
            zone: ZoneId,
        ): MileageReport {
            val rows = trips
                .filter { YearMonth.from(it.startTimeUtc.atZone(zone)) == period }
                .sortedBy { it.startTimeUtc }
                .map { trip ->
                    val miles = effectiveMiles(trip).roundToInt()
                    ReportRow(
                        date = trip.startTimeUtc.atZone(zone).toLocalDate(),
                        locationAndExplanation = locationLine(trip),
                        miles = miles,
                        amount = roundCents(miles * profile.ratePerMile),
                    )
                }
            return MileageReport(period, profile, rows)
        }

        private fun effectiveMiles(trip: Trip): Double =
            (trip.distanceCorrectionMeters ?: trip.distanceMeters) / METERS_PER_MILE

        private fun locationLine(trip: Trip): String =
            listOfNotNull(
                trip.destinationLabel?.takeIf { it.isNotBlank() },
                trip.purpose?.takeIf { it.isNotBlank() },
            ).joinToString(" — ")

        private fun roundCents(v: Double): Double = (v * 100).roundToLong() / 100.0
    }
}
