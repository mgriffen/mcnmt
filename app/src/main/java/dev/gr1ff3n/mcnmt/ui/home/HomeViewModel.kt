package dev.gr1ff3n.mcnmt.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gr1ff3n.mcnmt.data.Trip
import dev.gr1ff3n.mcnmt.data.TripRepository
import dev.gr1ff3n.mcnmt.data.settings.MileageProfile
import dev.gr1ff3n.mcnmt.data.settings.SettingsRepository
import dev.gr1ff3n.mcnmt.domain.report.MileageReport
import dev.gr1ff3n.mcnmt.service.TripController
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class HomeViewModel @Inject constructor(
    repository: TripRepository,
    settings: SettingsRepository,
    private val tripController: TripController,
) : ViewModel() {

    private val zone: ZoneId = ZoneId.systemDefault()

    val activeTrip: StateFlow<Trip?> = repository.observeActiveTrip()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Current calendar month's reimbursable total, for the Home summary card. */
    val monthReport: StateFlow<MileageReport> = combine(
        repository.observeTrips(),
        settings.profile,
    ) { trips, profile ->
        MileageReport.build(trips, YearMonth.now(zone), profile, zone)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        MileageReport(YearMonth.now(zone), MileageProfile(), emptyList()),
    )

    fun startManualTrip() = tripController.startManual()
    fun stopManualTrip() = tripController.stopManual()
}
