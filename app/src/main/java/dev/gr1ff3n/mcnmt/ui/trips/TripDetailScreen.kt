package dev.gr1ff3n.mcnmt.ui.trips

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.gr1ff3n.mcnmt.data.Trip
import dev.gr1ff3n.mcnmt.ui.components.MileageScaffold
import dev.gr1ff3n.mcnmt.ui.components.mileageFieldColors
import dev.gr1ff3n.mcnmt.ui.theme.AccentOrange
import dev.gr1ff3n.mcnmt.ui.theme.GlassBorder
import dev.gr1ff3n.mcnmt.ui.theme.SleekText
import dev.gr1ff3n.mcnmt.domain.model.TripCategory
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val METERS_PER_MILE = 1609.344

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    onBack: () -> Unit,
    viewModel: TripDetailViewModel = hiltViewModel(),
) {
    val trip by viewModel.trip.collectAsStateWithLifecycle()
    val saving by viewModel.saving.collectAsStateWithLifecycle()
    val isNew = viewModel.isNew

    MileageScaffold(title = if (isNew) "Add trip" else "Edit trip", onBack = onBack) { padding ->
        when (val t = trip) {
            null -> Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            else -> TripDetailBody(
                trip = t,
                isNew = isNew,
                saving = saving,
                modifier = Modifier.padding(padding),
                onSave = { start, end, miles, category, purpose, dest, notes ->
                    viewModel.save(start, end, miles, category, purpose, dest, notes, onSaved = onBack)
                },
                onDelete = { viewModel.delete(onDeleted = onBack) },
            )
        }
    }
}

@Composable
private fun TripDetailBody(
    trip: Trip,
    isNew: Boolean,
    saving: Boolean,
    modifier: Modifier = Modifier,
    onSave: (Instant, Instant?, Double, TripCategory, String?, String?, String?) -> Unit,
    onDelete: () -> Unit = {},
) {
    val zone = remember { ZoneId.systemDefault() }
    val startZdt = remember(trip.id) { trip.startTimeUtc.atZone(zone) }

    var date by remember(trip.id) { mutableStateOf(startZdt.toLocalDate()) }
    var startTime by remember(trip.id) { mutableStateOf(startZdt.toLocalTime()) }
    var endTime by remember(trip.id) {
        mutableStateOf(trip.endTimeUtc?.atZone(zone)?.toLocalTime())
    }
    var milesText by remember(trip.id) {
        val effective = (trip.distanceCorrectionMeters ?: trip.distanceMeters) / METERS_PER_MILE
        mutableStateOf(if (isNew && effective == 0.0) "" else trimMiles(effective))
    }
    // Category is no longer surfaced (every recorded trip is reimbursable work);
    // preserve whatever is stored so saving doesn't change it.
    val category = trip.category
    var purpose by remember(trip.id) { mutableStateOf(trip.purpose.orEmpty()) }
    var destinationLabel by remember(trip.id) { mutableStateOf(trip.destinationLabel.orEmpty()) }
    var notes by remember(trip.id) { mutableStateOf(trip.notes.orEmpty()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionTitle("Date & time")
        DateField(date = date, onDateChange = { date = it })
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TimeField(
                label = "Start",
                time = startTime,
                modifier = Modifier.weight(1f),
                onTimeChange = { startTime = it },
            )
            TimeField(
                label = "End",
                time = endTime,
                modifier = Modifier.weight(1f),
                onTimeChange = { endTime = it },
            )
        }

        SectionTitle("Miles")
        OutlinedTextField(
            value = milesText,
            onValueChange = { milesText = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("e.g. 62") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            suffix = { Text("mi") },
            colors = mileageFieldColors(),
        )
        if (!isNew && trip.distanceCorrectionMeters != null) {
            Text(
                text = "GPS measured %s mi".format(trimMiles(trip.distanceMeters / METERS_PER_MILE)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SectionTitle("Location of travel & explanation")
        OutlinedTextField(
            value = destinationLabel,
            onValueChange = { destinationLabel = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("e.g. Office → County Admin, Ukiah") },
            singleLine = true,
            colors = mileageFieldColors(),
        )

        SectionTitle("Purpose")
        OutlinedTextField(
            value = purpose,
            onValueChange = { purpose = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("e.g. Vendor meeting") },
            singleLine = true,
            colors = mileageFieldColors(),
        )

        SectionTitle("Notes")
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Any extra details") },
            minLines = 3,
            colors = mileageFieldColors(),
        )

        val hasGps = trip.startLatitude != null || trip.endLatitude != null
        if (!isNew && hasGps) {
            HorizontalDivider()
            ReadOnlyDetails(trip)
        }

        Button(
            onClick = {
                val miles = milesText.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
                val startInstant = date.atTime(startTime).atZone(zone).toInstant()
                val endInstant = endTime?.let { date.atTime(it).atZone(zone).toInstant() }
                onSave(startInstant, endInstant, miles, category, purpose, destinationLabel, notes)
            },
            enabled = !saving,
            colors = ButtonDefaults.buttonColors(containerColor = AccentOrange, contentColor = Color.White),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (saving) "Saving…" else "Save")
        }

        if (!isNew) {
            var confirmDelete by remember { mutableStateOf(false) }
            TextButton(
                onClick = { confirmDelete = true },
                enabled = !saving,
                colors = ButtonDefaults.textButtonColors(contentColor = DeleteRed),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Delete trip")
            }
            if (confirmDelete) {
                AlertDialog(
                    onDismissRequest = { confirmDelete = false },
                    title = { Text("Delete this trip?") },
                    text = { Text("This permanently removes the trip and its GPS track. This can't be undone.") },
                    confirmButton = {
                        TextButton(
                            onClick = { confirmDelete = false; onDelete() },
                            colors = ButtonDefaults.textButtonColors(contentColor = DeleteRed),
                        ) { Text("Delete") }
                    },
                    dismissButton = {
                        TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
                    },
                )
            }
        }
    }
}

private val DeleteRed = Color(0xFFFF5A3C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(date: LocalDate, onDateChange: (LocalDate) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    OutlinedButton(
        onClick = { showPicker = true },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = SleekText),
        border = BorderStroke(1.dp, GlassBorder),
    ) {
        Text(date.format(DATE_FMT))
    }
    if (showPicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        // Picker reports UTC midnight — read the date back in UTC.
                        onDateChange(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Cancel") } },
        ) {
            DatePicker(state = state)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeField(
    label: String,
    time: LocalTime?,
    modifier: Modifier = Modifier,
    onTimeChange: (LocalTime) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }
    OutlinedButton(
        onClick = { showPicker = true },
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = SleekText),
        border = BorderStroke(1.dp, GlassBorder),
    ) {
        Text(time?.format(TIME_FMT) ?: "$label —")
    }
    if (showPicker) {
        val initial = time ?: LocalTime.NOON
        val state = rememberTimePickerState(
            initialHour = initial.hour,
            initialMinute = initial.minute,
            is24Hour = false,
        )
        Dialog(onDismissRequest = { showPicker = false }) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text("Select $label time", style = MaterialTheme.typography.titleMedium)
                    TimePicker(state = state)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = { showPicker = false }) { Text("Cancel") }
                        TextButton(onClick = {
                            onTimeChange(LocalTime.of(state.hour, state.minute))
                            showPicker = false
                        }) { Text("OK") }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReadOnlyDetails(trip: Trip) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        SectionTitle("GPS track")
        if (trip.startLatitude != null && trip.startLongitude != null) {
            DetailRow("Start", "%.5f, %.5f".format(trip.startLatitude, trip.startLongitude))
        }
        if (trip.endLatitude != null && trip.endLongitude != null) {
            DetailRow("End", "%.5f, %.5f".format(trip.endLatitude, trip.endLongitude))
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/** Drops a trailing ".0" so whole miles read "62" not "62.0". */
private fun trimMiles(miles: Double): String =
    if (miles == miles.toLong().toDouble()) miles.toLong().toString()
    else "%.1f".format(miles)

private val DATE_FMT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.getDefault())
private val TIME_FMT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
