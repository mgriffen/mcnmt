package dev.gr1ff3n.mcnmt.ui.trips

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gr1ff3n.mcnmt.data.Trip
import dev.gr1ff3n.mcnmt.data.TripRepository
import dev.gr1ff3n.mcnmt.domain.model.TripCategory
import dev.gr1ff3n.mcnmt.domain.model.TripSource
import java.time.Instant
import javax.inject.Inject
import kotlin.math.abs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class TripDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: TripRepository,
) : ViewModel() {

    /** tripId <= 0 means "create a new manual trip". */
    val tripId: Long = savedStateHandle.get<Long>("tripId") ?: NEW_TRIP_ID
    val isNew: Boolean = tripId <= 0L

    private val _trip = MutableStateFlow<Trip?>(null)
    val trip: StateFlow<Trip?> = _trip.asStateFlow()

    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()

    init {
        viewModelScope.launch {
            _trip.value = if (isNew) blankTemplate() else repository.getTrip(tripId)
        }
    }

    private suspend fun blankTemplate(): Trip {
        val vehicle = repository.getDefaultVehicle()
        val now = Instant.now()
        return Trip(
            vehicleId = vehicle?.id ?: 0L,
            startTimeUtc = now,
            endTimeUtc = now,
            distanceMeters = 0.0,
            source = TripSource.MANUAL,
            category = TripCategory.WORK,
        )
    }

    /**
     * Persists the edited values. [miles] is the human-facing mileage:
     * - For a new manual trip it becomes the trip distance directly.
     * - For an existing (GPS) trip it is stored as a correction only when it
     *   differs from the GPS-measured distance, so an untouched trip keeps a
     *   null correction.
     */
    fun save(
        startTimeUtc: Instant,
        endTimeUtc: Instant?,
        miles: Double,
        category: TripCategory,
        purpose: String?,
        destinationLabel: String?,
        notes: String?,
        onSaved: () -> Unit,
    ) {
        val current = _trip.value ?: return
        viewModelScope.launch {
            _saving.value = true
            val enteredMeters = miles * METERS_PER_MILE
            val distanceMeters: Double
            val correction: Double?
            if (isNew) {
                distanceMeters = enteredMeters
                correction = null
            } else {
                distanceMeters = current.distanceMeters
                correction = if (abs(enteredMeters - current.distanceMeters) < 0.5) null
                             else enteredMeters
            }
            val updated = current.copy(
                startTimeUtc = startTimeUtc,
                endTimeUtc = endTimeUtc,
                distanceMeters = distanceMeters,
                distanceCorrectionMeters = correction,
                category = category,
                purpose = purpose?.takeIf { it.isNotBlank() },
                destinationLabel = destinationLabel?.takeIf { it.isNotBlank() },
                notes = notes?.takeIf { it.isNotBlank() },
            )
            if (isNew) repository.insertTrip(updated) else repository.updateTrip(updated)
            _trip.value = updated
            _saving.value = false
            onSaved()
        }
    }

    fun delete(onDeleted: () -> Unit) {
        val current = _trip.value ?: return
        if (isNew) return // nothing persisted yet
        viewModelScope.launch {
            _saving.value = true
            repository.deleteTrip(current)
            _saving.value = false
            onDeleted()
        }
    }

    companion object {
        const val NEW_TRIP_ID = -1L
        private const val METERS_PER_MILE = 1609.344
    }
}
