package dev.gr1ff3n.mcnmt.ui.trips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gr1ff3n.mcnmt.data.Trip
import dev.gr1ff3n.mcnmt.data.TripRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class TripListViewModel @Inject constructor(
    repository: TripRepository,
) : ViewModel() {
    val trips: StateFlow<List<Trip>> = repository.observeTrips()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
