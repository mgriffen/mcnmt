package dev.gr1ff3n.mcnmt.ui.trips

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gr1ff3n.mcnmt.data.Trip
import dev.gr1ff3n.mcnmt.data.TripRepository
import dev.gr1ff3n.mcnmt.domain.model.TripCategory
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class TripDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: TripRepository,
) : ViewModel() {

    val tripId: Long = savedStateHandle.get<Long>("tripId") ?: -1L

    private val _trip = MutableStateFlow<Trip?>(null)
    val trip: StateFlow<Trip?> = _trip.asStateFlow()

    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()

    init {
        viewModelScope.launch {
            _trip.value = repository.getTrip(tripId)
        }
    }

    fun save(
        category: TripCategory,
        purpose: String?,
        destinationLabel: String?,
        notes: String?,
        onSaved: () -> Unit,
    ) {
        val current = _trip.value ?: return
        viewModelScope.launch {
            _saving.value = true
            val updated = current.copy(
                category = category,
                purpose = purpose?.takeIf { it.isNotBlank() },
                destinationLabel = destinationLabel?.takeIf { it.isNotBlank() },
                notes = notes?.takeIf { it.isNotBlank() },
            )
            repository.updateTrip(updated)
            _trip.value = updated
            _saving.value = false
            onSaved()
        }
    }
}
