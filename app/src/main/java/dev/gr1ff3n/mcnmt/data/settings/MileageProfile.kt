package dev.gr1ff3n.mcnmt.data.settings

/**
 * User-stable values that pre-fill every reimbursement voucher. Entered once
 * in Settings and reused on every report, so the per-trip flow stays minimal.
 *
 * The mileage [ratePerMile] is stored as dollars per mile and year-stamped via
 * [rateLabel] because the City prints the effective date on the form (e.g. the
 * 2026 IRS business standard is $0.725/mi). When the IRS rate changes, only
 * these two fields move.
 */
data class MileageProfile(
    val employeeName: String = "",
    val address: String = "",
    val department: String = "",
    val accountNumber: String = "",
    val travelerTitle: String = "",
    val ratePerMile: Double = DEFAULT_RATE_PER_MILE,
    val rateLabel: String = DEFAULT_RATE_LABEL,
) {
    companion object {
        /** 2026 IRS business standard mileage rate (72.5¢/mi). */
        const val DEFAULT_RATE_PER_MILE: Double = 0.725
        const val DEFAULT_RATE_LABEL: String = "2026 IRS standard"
    }
}
