package dev.gr1ff3n.mcnmt.ui.report

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gr1ff3n.mcnmt.data.TripRepository
import dev.gr1ff3n.mcnmt.data.export.ReportExporter
import dev.gr1ff3n.mcnmt.data.settings.MileageProfile
import dev.gr1ff3n.mcnmt.data.settings.SettingsRepository
import dev.gr1ff3n.mcnmt.domain.report.MileageReport
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ReportUiState(
    val availableMonths: List<YearMonth>,
    val selected: YearMonth,
    val report: MileageReport,
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    repository: TripRepository,
    settings: SettingsRepository,
    private val exporter: ReportExporter,
) : ViewModel() {

    private val zone: ZoneId = ZoneId.systemDefault()
    private val selectedMonth = MutableStateFlow<YearMonth?>(null)

    val state: StateFlow<ReportUiState> = combine(
        repository.observeTrips(),
        settings.profile,
        selectedMonth,
    ) { trips, profile, sel ->
        val months = trips
            .map { YearMonth.from(it.startTimeUtc.atZone(zone)) }
            .distinct()
            .sortedDescending()
        val month = sel ?: months.firstOrNull() ?: YearMonth.now(zone)
        ReportUiState(
            availableMonths = months,
            selected = month,
            report = MileageReport.build(trips, month, profile, zone),
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyState(zone),
    )

    fun selectMonth(month: YearMonth) {
        selectedMonth.value = month
    }

    suspend fun exportCsv(): Uri = exporter.exportCsv(state.value.report)

    suspend fun voucherHtml(): String = exporter.buildVoucherHtml(state.value.report)

    private companion object {
        fun emptyState(zone: ZoneId): ReportUiState {
            val now = YearMonth.now(zone)
            return ReportUiState(emptyList(), now, MileageReport(now, MileageProfile(), emptyList()))
        }
    }
}
