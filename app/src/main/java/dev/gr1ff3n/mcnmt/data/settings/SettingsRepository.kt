package dev.gr1ff3n.mcnmt.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Reads/writes the [MileageProfile] backed by a Preferences DataStore.
 * Exposes a single [profile] flow that always emits a fully-defaulted profile
 * (missing keys fall back to [MileageProfile] defaults), so callers never deal
 * with nulls.
 */
@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val profile: Flow<MileageProfile> = dataStore.data.map { prefs ->
        MileageProfile(
            employeeName = prefs[Keys.EMPLOYEE_NAME] ?: "",
            address = prefs[Keys.ADDRESS] ?: "",
            department = prefs[Keys.DEPARTMENT] ?: "",
            accountNumber = prefs[Keys.ACCOUNT_NUMBER] ?: "",
            travelerTitle = prefs[Keys.TRAVELER_TITLE] ?: "",
            ratePerMile = prefs[Keys.RATE_PER_MILE] ?: MileageProfile.DEFAULT_RATE_PER_MILE,
            rateLabel = prefs[Keys.RATE_LABEL] ?: MileageProfile.DEFAULT_RATE_LABEL,
        )
    }

    suspend fun update(profile: MileageProfile) {
        dataStore.edit { prefs ->
            prefs[Keys.EMPLOYEE_NAME] = profile.employeeName
            prefs[Keys.ADDRESS] = profile.address
            prefs[Keys.DEPARTMENT] = profile.department
            prefs[Keys.ACCOUNT_NUMBER] = profile.accountNumber
            prefs[Keys.TRAVELER_TITLE] = profile.travelerTitle
            prefs[Keys.RATE_PER_MILE] = profile.ratePerMile
            prefs[Keys.RATE_LABEL] = profile.rateLabel
        }
    }

    private object Keys {
        val EMPLOYEE_NAME = stringPreferencesKey("employee_name")
        val ADDRESS = stringPreferencesKey("address")
        val DEPARTMENT = stringPreferencesKey("department")
        val ACCOUNT_NUMBER = stringPreferencesKey("account_number")
        val TRAVELER_TITLE = stringPreferencesKey("traveler_title")
        val RATE_PER_MILE = doublePreferencesKey("rate_per_mile")
        val RATE_LABEL = stringPreferencesKey("rate_label")
    }
}
