package dev.gr1ff3n.mcnmt.domain.report

import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Renders a [MileageReport] as CSV. A small key/value header block (employee,
 * period, rate, …) precedes the trip table so the file is self-describing when
 * opened in Excel/Sheets. Amounts are plain numbers (no `$`) to stay numeric.
 */
object CsvBuilder {

    private val DATE_FMT: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
    private val PERIOD_FMT: DateTimeFormatter =
        DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US)

    fun build(report: MileageReport): String = buildString {
        val p = report.profile
        appendLine("MCN Mileage Reimbursement")
        appendLine("${esc("Employee")},${esc(p.employeeName)}")
        appendLine("${esc("Department")},${esc(p.department)}")
        appendLine("${esc("Account")},${esc(p.accountNumber)}")
        appendLine("${esc("Period")},${esc(report.period.format(PERIOD_FMT))}")
        appendLine("${esc("Rate")},${esc("$%.3f/mi (%s)".format(p.ratePerMile, p.rateLabel))}")
        appendLine()
        appendLine(
            listOf("Date", "Location of Travel & Explanation", "Miles", "Amount")
                .joinToString(",") { esc(it) }
        )
        report.rows.forEach { row ->
            appendLine(
                listOf(
                    esc(row.date.format(DATE_FMT)),
                    esc(row.locationAndExplanation),
                    row.miles.toString(),
                    "%.2f".format(row.amount),
                ).joinToString(",")
            )
        }
        appendLine(
            listOf(esc("TOTALS"), "", report.totalMiles.toString(), "%.2f".format(report.totalAmount))
                .joinToString(",")
        )
    }

    /** Quote a field when it contains a comma, quote, or newline; double internal quotes. */
    private fun esc(value: String): String {
        if (value.isEmpty()) return ""
        val needsQuote = value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }
        val escaped = value.replace("\"", "\"\"")
        return if (needsQuote) "\"$escaped\"" else escaped
    }
}
