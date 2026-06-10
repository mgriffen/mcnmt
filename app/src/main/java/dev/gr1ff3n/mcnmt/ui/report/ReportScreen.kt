package dev.gr1ff3n.mcnmt.ui.report

import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.gr1ff3n.mcnmt.ui.components.GlassCard
import dev.gr1ff3n.mcnmt.ui.components.MileageScaffold
import dev.gr1ff3n.mcnmt.ui.theme.AccentOrange
import dev.gr1ff3n.mcnmt.ui.theme.AccentOrangeBright
import dev.gr1ff3n.mcnmt.ui.theme.FieldBorder
import dev.gr1ff3n.mcnmt.ui.theme.SleekText
import dev.gr1ff3n.mcnmt.ui.theme.SleekTextDim
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

private val MONTH_FMT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
private val ROW_DATE_FMT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())

@Composable
fun ReportScreen(
    onBack: () -> Unit,
    viewModel: ReportViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    // Hold the print WebView so it isn't GC'd before the print job is handed off.
    val printWebView = remember { mutableStateOf<WebView?>(null) }

    MileageScaffold(title = "Monthly report", onBack = onBack) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            MonthSelector(
                selected = state.selected,
                available = state.availableMonths,
                onSelect = viewModel::selectMonth,
            )

            SummaryCard(
                miles = state.report.totalMiles,
                amount = state.report.totalAmount,
                tripCount = state.report.rows.size,
            )

            if (state.report.rows.isEmpty()) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        "No trips in ${state.selected.format(MONTH_FMT)}.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SleekTextDim,
                    )
                }
            } else {
                GlassCard(modifier = Modifier.weight(1f), contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(state.report.rows) { row ->
                            RowItem(
                                date = row.date.format(ROW_DATE_FMT),
                                label = row.locationAndExplanation.ifBlank { "(no description)" },
                                miles = row.miles,
                                amount = row.amount,
                            )
                            HorizontalDivider(color = Color(0x12FFFFFF))
                        }
                    }
                }
            }

            val canExport = state.report.rows.isNotEmpty() && !busy
            val periodLabel = state.selected.format(MONTH_FMT)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        scope.launch {
                            busy = true
                            try {
                                val html = viewModel.voucherHtml()
                                printVoucher(context, html, "MCN Mileage — $periodLabel") {
                                    printWebView.value = it
                                }
                            } finally { busy = false }
                        }
                    },
                    enabled = canExport,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange, contentColor = Color.White),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Filled.PictureAsPdf, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (busy) "Preparing…" else "PDF")
                }
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            busy = true
                            try {
                                shareUri(context, viewModel.exportCsv(), "text/csv", periodLabel)
                            } finally { busy = false }
                        }
                    },
                    enabled = canExport,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SleekText),
                    border = BorderStroke(1.dp, FieldBorder),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("CSV")
                }
            }
        }
    }
}

@Composable
private fun MonthSelector(
    selected: YearMonth,
    available: List<YearMonth>,
    onSelect: (YearMonth) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        GlassCard(
            modifier = Modifier.fillMaxWidth().clickable(enabled = available.isNotEmpty()) { expanded = true },
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(selected.format(MONTH_FMT), style = MaterialTheme.typography.titleMedium, color = SleekText, modifier = Modifier.weight(1f))
                Icon(Icons.Filled.ArrowDropDown, contentDescription = "Choose month", tint = SleekTextDim)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            available.forEach { month ->
                DropdownMenuItem(
                    text = { Text(month.format(MONTH_FMT)) },
                    onClick = { onSelect(month); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(miles: Int, amount: Double, tripCount: Int) {
    GlassCard(strong = true, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Stat("Trips", tripCount.toString())
            Stat("Miles", miles.toString())
            Stat("Reimbursement", "$%.2f".format(amount), accent = true)
        }
    }
}

@Composable
private fun Stat(label: String, value: String, accent: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = if (accent) AccentOrangeBright else SleekText)
        Text(label, style = MaterialTheme.typography.labelMedium, color = SleekTextDim)
    }
}

@Composable
private fun RowItem(date: String, label: String, miles: Int, amount: Double) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Date sized to content; label takes the slack; miles/amount never wrap.
        Text(
            date,
            style = MaterialTheme.typography.bodySmall,
            color = SleekTextDim,
            maxLines = 1,
            softWrap = false,
        )
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = SleekText,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text(
            "$miles mi",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = SleekText,
            maxLines = 1,
            softWrap = false,
            textAlign = TextAlign.End,
        )
        Text(
            "$%.2f".format(amount),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = AccentOrange,
            maxLines = 1,
            softWrap = false,
            textAlign = TextAlign.End,
            modifier = Modifier.widthIn(min = 64.dp),
        )
    }
}

private fun shareUri(
    context: android.content.Context,
    uri: android.net.Uri,
    mime: String,
    periodLabel: String,
) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mime
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "MCN Mileage — $periodLabel")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share report"))
}

/**
 * Loads the voucher HTML into an offscreen WebView and hands it to the platform
 * print pipeline. Android shows its print preview, where the user picks
 * "Save as PDF" (or a printer). [hold] keeps the WebView alive until handoff.
 */
private fun printVoucher(
    context: android.content.Context,
    html: String,
    jobName: String,
    hold: (WebView) -> Unit,
) {
    val webView = WebView(context)
    webView.webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String?) {
            val printManager = context.getSystemService(android.content.Context.PRINT_SERVICE) as PrintManager
            val attributes = PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.NA_LETTER)
                .build()
            printManager.print(jobName, view.createPrintDocumentAdapter(jobName), attributes)
        }
    }
    hold(webView)
    webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
}
