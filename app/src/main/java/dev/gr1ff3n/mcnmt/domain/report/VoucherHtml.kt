package dev.gr1ff3n.mcnmt.domain.report

import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Renders a [MileageReport] as a self-contained HTML voucher (City of Fort
 * Bragg + MCN Broadband mileage reimbursement). Pure and Android-free: the two
 * logos are passed in as data: URIs so this can be unit-tested and previewed in
 * any browser. Designed for letter-size print — a WebView turns it into the PDF.
 *
 * Pagination is handled by CSS: the `<thead>` repeats on every printed page and
 * long description cells wrap automatically, so an overflowing month flows onto
 * continuation pages with no manual layout math.
 */
object VoucherHtml {

    private val ROW_DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("M/d/yyyy", Locale.US)
    private val PERIOD: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US)

    fun build(report: MileageReport, fortBraggLogo: String, mcnLogo: String): String {
        val p = report.profile
        val rowsHtml = report.rows.mapIndexed { i, row ->
            """
            <tr>
              <td class="num">${i + 1}</td>
              <td class="date">${esc(row.date.format(ROW_DATE))}</td>
              <td class="loc">${esc(row.locationAndExplanation)}</td>
              <td class="miles">${row.miles}</td>
              <td class="amt">${money(row.amount)}</td>
            </tr>
            """.trimIndent()
        }.joinToString("\n")

        return """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<style>
  @page { size: letter; margin: 0.5in; }
  * { box-sizing: border-box; }
  body {
    font-family: Arial, Helvetica, sans-serif;
    color: #1a1c1b; font-size: 11px; margin: 0;
  }
  .header { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
  .header img { height: 64px; width: auto; }
  .title { text-align: center; flex: 1; }
  .title h1 { margin: 0; font-size: 16px; color: #003E52; letter-spacing: .3px; }
  .title .addr { margin-top: 2px; color: #45504c; font-size: 10px; }
  hr { border: none; border-top: 2px solid #003E52; margin: 8px 0 10px; }

  .info { display: grid; grid-template-columns: 1fr 1fr; gap: 2px 24px; margin-bottom: 6px; }
  .info .field { display: flex; gap: 6px; padding: 2px 0; }
  .info .label { color: #45504c; font-weight: bold; white-space: nowrap; }
  .info .value { flex: 1; border-bottom: 1px solid #b8c2bd; min-height: 14px; }
  .purpose { margin: 6px 0 12px; font-size: 11px; color: #2b3a40; }
  .purpose .plabel { color: #45504c; font-weight: bold; margin-right: 6px; }

  table { width: 100%; border-collapse: collapse; }
  thead { display: table-header-group; }
  th, td { border: 1px solid #9aa6a1; padding: 4px 6px; vertical-align: top; }
  th { background: #003E52; color: #fff; font-size: 10px; text-align: left; }
  td.num, th.num { width: 24px; text-align: center; }
  td.date, th.date { width: 74px; white-space: nowrap; }
  td.miles, th.miles { width: 52px; text-align: right; }
  td.amt, th.amt { width: 72px; text-align: right; }
  td.loc { word-break: break-word; }
  tr.totals td { font-weight: bold; background: #eef3f1; }
  tr.totals .lbl { text-align: right; }

  .ratestamp { margin: 6px 0 14px; color: #45504c; font-style: italic; }
  .certify { font-size: 10px; color: #303533; margin-bottom: 14px; }
  .sign { display: grid; grid-template-columns: 2fr 1fr 1fr; gap: 18px; margin-top: 22px; }
  .sign .line { border-top: 1px solid #45504c; padding-top: 3px; color: #45504c; font-size: 10px; }
</style>
</head>
<body>

  <div class="header">
    <img src="$fortBraggLogo" alt="City of Fort Bragg">
    <div class="title">
      <h1>MILEAGE REIMBURSEMENT VOUCHER</h1>
      <div class="addr">City of Fort Bragg &middot; 416 N. Franklin Street &middot; Fort Bragg, CA 95437</div>
    </div>
    <img src="$mcnLogo" alt="MCN Broadband">
  </div>
  <hr>

  <div class="info">
    <div class="field"><span class="label">Name:</span><span class="value">${esc(p.employeeName)}</span></div>
    <div class="field"><span class="label">Department:</span><span class="value">${esc(p.department)}</span></div>
    <div class="field"><span class="label">Address:</span><span class="value">${esc(p.address)}</span></div>
    <div class="field"><span class="label">Period:</span><span class="value">${esc(report.period.format(PERIOD))}</span></div>
    <div class="field"><span class="label">Charge to account #:</span><span class="value">${esc(p.accountNumber)}</span></div>
    <div class="field"><span class="label">Title:</span><span class="value">${esc(p.travelerTitle)}</span></div>
  </div>

  <div class="purpose"><span class="plabel">Purpose:</span>Use of personal vehicle for work duties</div>

  <table>
    <thead>
      <tr>
        <th class="num">#</th>
        <th class="date">Date</th>
        <th class="loc">Location of Travel &amp; Explanation</th>
        <th class="miles">Miles</th>
        <th class="amt">Amount</th>
      </tr>
    </thead>
    <tbody>
$rowsHtml
      <tr class="totals">
        <td colspan="3" class="lbl">TOTALS</td>
        <td class="miles">${report.totalMiles}</td>
        <td class="amt">${money(report.totalAmount)}</td>
      </tr>
    </tbody>
  </table>

  <div class="ratestamp">Rate: ${rate(p.ratePerMile)} per mile (${esc(p.rateLabel)})</div>

  <div class="certify">I certify all computations are correct and that the travel above was incurred on official business of the City of Fort Bragg.</div>

  <div class="sign">
    <div class="line">Traveler signature</div>
    <div class="line">Date</div>
    <div class="line">Title</div>
  </div>
  <div class="sign">
    <div class="line">Supervisor signature (approved)</div>
    <div class="line">Date</div>
    <div class="line">&nbsp;</div>
  </div>

</body>
</html>
        """.trimIndent()
    }

    private fun money(v: Double): String = "$" + "%.2f".format(v)

    /** Rate needs 3 decimals so $0.725 doesn't round to $0.72. */
    private fun rate(v: Double): String = "$" + "%.3f".format(v)

    private fun esc(s: String): String = buildString {
        for (c in s) when (c) {
            '&' -> append("&amp;")
            '<' -> append("&lt;")
            '>' -> append("&gt;")
            '"' -> append("&quot;")
            else -> append(c)
        }
    }
}
