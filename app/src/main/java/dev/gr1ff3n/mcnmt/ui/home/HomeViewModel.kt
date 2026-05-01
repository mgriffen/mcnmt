package dev.gr1ff3n.mcnmt.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gr1ff3n.mcnmt.data.TripRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class HomeViewModel @Inject constructor(
    repository: TripRepository,
) : ViewModel() {
    val tripCount: StateFlow<Int> = repository.observeTripCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
}
