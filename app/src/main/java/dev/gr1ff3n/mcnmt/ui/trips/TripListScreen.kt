package dev.gr1ff3n.mcnmt.ui.trips

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.gr1ff3n.mcnmt.data.Trip
import dev.gr1ff3n.mcnmt.ui.components.GlassCard
import dev.gr1ff3n.mcnmt.ui.components.MileageScaffold
import dev.gr1ff3n.mcnmt.ui.theme.AccentOrange
import dev.gr1ff3n.mcnmt.ui.theme.SleekText
import dev.gr1ff3n.mcnmt.ui.theme.SleekTextDim
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TripListScreen(
    onBack: () -> Unit,
    onTripClick: (Long) -> Unit,
    onAddTrip: () -> Unit = {},
    viewModel: TripListViewModel = hiltViewModel(),
) {
    val trips by viewModel.trips.collectAsStateWithLifecycle()

    MileageScaffold(
        title = "Trips",
        onBack = onBack,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTrip,
                containerColor = AccentOrange,
                contentColor = Color.White,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Add trip") },
            )
        },
    ) { padding ->
        if (trips.isEmpty()) {
            EmptyState(modifier = Modifier.padding(padding).fillMaxSize())
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(trips, key = { it.id }) { trip ->
                    TripRow(trip = trip, onClick = { onTripClick(trip.id) })
                }
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No trips yet", style = MaterialTheme.typography.titleMedium, color = SleekText)
            Text(
                "Trips you record will appear here.",
                style = MaterialTheme.typography.bodyMedium,
                color = SleekTextDim,
            )
        }
    }
}

@Composable
private fun TripRow(trip: Trip, onClick: () -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = trip.destinationLabel?.takeIf { it.isNotBlank() } ?: formatDate(trip.startTimeUtc),
                    style = MaterialTheme.typography.titleMedium,
                    color = SleekText,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${formatDate(trip.startTimeUtc)} · ${formatTripWindow(trip.startTimeUtc, trip.endTimeUtc)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SleekTextDim,
                )
                if (trip.endTimeUtc == null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x1FFF9A44))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text(
                            "IN PROGRESS",
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentOrange,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                val miles = (trip.distanceCorrectionMeters ?: trip.distanceMeters) / 1609.344
                Text(
                    text = "%.1f".format(miles),
                    fontWeight = FontWeight.Bold,
                    color = AccentOrange,
                    style = MaterialTheme.typography.titleLarge,
                )
                Text("miles", style = MaterialTheme.typography.labelSmall, color = SleekTextDim)
            }
        }
    }
}

private fun formatDate(instant: Instant): String =
    instant.atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault()))

private fun formatTripWindow(start: Instant, end: Instant?): String {
    val fmt = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
    val zone = ZoneId.systemDefault()
    val s = start.atZone(zone).format(fmt)
    val e = end?.atZone(zone)?.format(fmt) ?: "—"
    return "$s – $e"
}
