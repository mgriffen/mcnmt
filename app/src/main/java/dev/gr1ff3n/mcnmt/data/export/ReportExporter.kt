package dev.gr1ff3n.mcnmt.data.export

import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gr1ff3n.mcnmt.R
import dev.gr1ff3n.mcnmt.domain.report.CsvBuilder
import dev.gr1ff3n.mcnmt.domain.report.MileageReport
import dev.gr1ff3n.mcnmt.domain.report.VoucherHtml
import java.io.File
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Writes a [MileageReport] to a shareable file in the app cache and returns a
 * content:// [Uri] via FileProvider, suitable for an ACTION_SEND share/email.
 */
@Singleton
class ReportExporter @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun exportCsv(report: MileageReport): Uri = write(
        fileName = baseName(report) + ".csv",
        content = CsvBuilder.build(report),
    )

    /**
     * Builds the self-contained voucher HTML (logos inlined as data: URIs). The
     * caller prints it to PDF via the platform print framework — Android has no
     * supported API to render HTML straight to a PDF file.
     */
    suspend fun buildVoucherHtml(report: MileageReport): String = withContext(Dispatchers.IO) {
        VoucherHtml.build(
            report = report,
            fortBraggLogo = dataUri(R.drawable.logo_fort_bragg, "image/jpeg"),
            mcnLogo = dataUri(R.drawable.logo_mcn_broadband, "image/png"),
        )
    }

    private suspend fun write(fileName: String, content: String): Uri = withContext(Dispatchers.IO) {
        val file = exportFile(fileName)
        file.writeText(content)
        uriFor(file)
    }

    private fun exportFile(fileName: String): File {
        val dir = File(context.cacheDir, EXPORT_DIR).apply { mkdirs() }
        return File(dir, fileName)
    }

    private fun uriFor(file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    /** Reads a bitmap drawable's raw bytes and encodes them as a data: URI. */
    private fun dataUri(resId: Int, mime: String): String {
        val bytes = context.resources.openRawResource(resId).use { it.readBytes() }
        return "data:$mime;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun baseName(report: MileageReport): String =
        "MCN-Mileage-" + report.period.format(FILE_PERIOD_FMT)

    companion object {
        const val EXPORT_DIR = "exports"
        private val FILE_PERIOD_FMT: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM", Locale.US)
    }
}
