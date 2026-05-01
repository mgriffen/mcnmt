package dev.gr1ff3n.mcnmt.ui.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.gr1ff3n.mcnmt.data.Trip
import dev.gr1ff3n.mcnmt.ui.theme.MCNMTTheme
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.flow.collect

private val requiredPermissions: List<String> = buildList {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        add(Manifest.permission.POST_NOTIFICATIONS)
    }
    add(Manifest.permission.ACCESS_FINE_LOCATION)
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val tripCount by viewModel.tripCount.collectAsStateWithLifecycle()
    val activeTrip by viewModel.activeTrip.collectAsStateWithLifecycle()

    var permissionsGranted by remember {
        mutableStateOf(
            requiredPermissions.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
        permissionsGranted = results.values.all { it }
    }

    // Re-check on each lifecycle resume — user may have toggled in Settings.
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.currentStateFlow
            .collect { lifecycleState ->
                if (lifecycleState.isAtLeast(Lifecycle.State.RESUMED)) {
                    permissionsGranted = requiredPermissions.all {
                        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                    }
                }
            }
    }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "MCNMT",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "MCN Mileage Tracker",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (!permissionsGranted) {
                PermissionBanner(onGrant = { permLauncher.launch(requiredPermissions.toTypedArray()) })
            }

            Spacer(modifier = Modifier.height(8.dp))

            ActiveTripCard(activeTrip = activeTrip)

            Button(
                onClick = {
                    if (activeTrip == null) viewModel.startManualTrip()
                    else viewModel.stopManualTrip()
                },
                enabled = permissionsGranted,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTrip != null) MaterialTheme.colorScheme.secondary
                                     else MaterialTheme.colorScheme.primary,
                    contentColor = if (activeTrip != null) MaterialTheme.colorScheme.onSecondary
                                   else MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text(if (activeTrip != null) "Stop trip" else "Start trip")
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Trips logged: $tripCount",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PermissionBanner(onGrant: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Permissions needed",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "MCNMT needs location and notification access to record trips.",
                style = MaterialTheme.typography.bodySmall,
            )
            Button(onClick = onGrant) { Text("Grant") }
        }
    }
}

@Composable
private fun ActiveTripCard(activeTrip: Trip?) {
    if (activeTrip == null) {
        Text(
            text = "No active trip",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        return
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "Tracking trip",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            val miles = activeTrip.distanceMeters / 1609.344
            Text(
                text = "%.2f mi · %s".format(
                    miles,
                    formatDuration(Duration.between(activeTrip.startTimeUtc, Instant.now())),
                ),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

private fun formatDuration(duration: Duration): String {
    val totalMinutes = duration.toMinutes().coerceAtLeast(0)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}

@Preview
@Composable
private fun HomeScreenPreview() {
    MCNMTTheme { HomeScreen() }
}
