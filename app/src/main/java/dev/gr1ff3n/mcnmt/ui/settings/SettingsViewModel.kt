package dev.gr1ff3n.mcnmt.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gr1ff3n.mcnmt.data.settings.MileageProfile
import dev.gr1ff3n.mcnmt.data.settings.SettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
) : ViewModel() {

    val profile: StateFlow<MileageProfile> = repository.profile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MileageProfile())

    fun save(profile: MileageProfile) {
        viewModelScope.launch { repository.update(profile) }
    }
}
