package dev.gr1ff3n.mcnmt.ui.trips

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.gr1ff3n.mcnmt.data.Trip
import dev.gr1ff3n.mcnmt.domain.model.TripCategory
import dev.gr1ff3n.mcnmt.domain.model.TripEndSource
import dev.gr1ff3n.mcnmt.domain.model.TripSource
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    onBack: () -> Unit,
    viewModel: TripDetailViewModel = hiltViewModel(),
) {
    val trip by viewModel.trip.collectAsStateWithLifecycle()
    val saving by viewModel.saving.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trip detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
    ) { padding ->
        when (val t = trip) {
            null -> Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            else -> TripDetailBody(
                trip = t,
                saving = saving,
                modifier = Modifier.padding(padding),
                onSave = { category, purpose, dest, notes ->
                    viewModel.save(category, purpose, dest, notes, onSaved = onBack)
                },
            )
        }
    }
}

@Composable
private fun TripDetailBody(
    trip: Trip,
    saving: Boolean,
    modifier: Modifier = Modifier,
    onSave: (TripCategory, String?, String?, String?) -> Unit,
) {
    var category by remember(trip.id) { mutableStateOf(trip.category) }
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
        HeaderCard(trip)

        SectionTitle("Category")
        CategoryChips(selected = category, onSelected = { category = it })

        SectionTitle("Purpose")
        OutlinedTextField(
            value = purpose,
            onValueChange = { purpose = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("e.g. Customer site visit") },
            singleLine = true,
        )

        SectionTitle("Destination")
        OutlinedTextField(
            value = destinationLabel,
            onValueChange = { destinationLabel = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("e.g. MCN office, 416 N Franklin") },
            singleLine = true,
        )

        SectionTitle("Notes")
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Any extra details") },
            minLines = 3,
        )

        HorizontalDivider()
        ReadOnlyDetails(trip)

        FilledTonalButton(
            onClick = { onSave(category, purpose, destinationLabel, notes) },
            enabled = !saving,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (saving) "Saving…" else "Save")
        }
    }
}

@Composable
private fun HeaderCard(trip: Trip) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = formatDate(trip.startTimeUtc),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = formatTripWindow(trip.startTimeUtc, trip.endTimeUtc),
                style = MaterialTheme.typography.bodyLarge,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Stat(
                    label = "Distance",
                    value = "%.2f mi".format(displayMiles(trip)),
                )
                trip.endTimeUtc?.let { end ->
                    Stat(
                        label = "Duration",
                        value = formatDuration(Duration.between(trip.startTimeUtc, end)),
                    )
                }
            }
            if (trip.distanceCorrectionMeters != null) {
                Text(
                    text = "Manually corrected · GPS measured %.2f mi"
                        .format(trip.distanceMeters / 1609.344),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryChips(
    selected: TripCategory,
    onSelected: (TripCategory) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TripCategory.entries.forEach { cat ->
            FilterChip(
                selected = cat == selected,
                onClick = { onSelected(cat) },
                label = { Text(cat.displayLabel()) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        }
    }
}

@Composable
private fun ReadOnlyDetails(trip: Trip) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        SectionTitle("Details")
        DetailRow("Source", trip.source.displayLabel())
        trip.endSource?.let { DetailRow("Ended via", it.displayLabel()) }
        if (trip.startLatitude != null && trip.startLongitude != null) {
            DetailRow("Start", "%.5f, %.5f".format(trip.startLatitude, trip.startLongitude))
        }
        if (trip.endLatitude != null && trip.endLongitude != null) {
            DetailRow("End", "%.5f, %.5f".format(trip.endLatitude, trip.endLongitude))
        }
        DetailRow("Internal id", trip.id.toString())
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

private fun TripCategory.displayLabel(): String = when (this) {
    TripCategory.UNREVIEWED -> "Unreviewed"
    TripCategory.WORK -> "Work"
    TripCategory.PERSONAL -> "Personal"
    TripCategory.LUNCH -> "Lunch"
}

private fun TripSource.displayLabel(): String = when (this) {
    TripSource.AUTO -> "Auto-detected"
    TripSource.MANUAL -> "Manual"
}

private fun TripEndSource.displayLabel(): String = when (this) {
    TripEndSource.AUTO_TIMEOUT -> "Auto stop (10 min stationary)"
    TripEndSource.MANUAL_STOP -> "Manual stop"
    TripEndSource.WORK_HOURS_ENDED -> "Work hours ended"
    TripEndSource.ORPHAN_RECOVERY -> "Recovered after crash"
}

private fun displayMiles(trip: Trip): Double =
    (trip.distanceCorrectionMeters ?: trip.distanceMeters) / 1609.344

private fun formatDate(instant: Instant): String =
    instant.atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy", Locale.getDefault()))

private fun formatTripWindow(start: Instant, end: Instant?): String {
    val fmt = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
    val zone = ZoneId.systemDefault()
    val s = start.atZone(zone).format(fmt)
    val e = end?.atZone(zone)?.format(fmt) ?: "in progress"
    return "$s – $e"
}

private fun formatDuration(d: Duration): String {
    val totalMinutes = d.toMinutes().coerceAtLeast(0)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}
