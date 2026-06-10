package dev.gr1ff3n.mcnmt.ui.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.gr1ff3n.mcnmt.R
import dev.gr1ff3n.mcnmt.data.Trip
import dev.gr1ff3n.mcnmt.domain.report.MileageReport
import dev.gr1ff3n.mcnmt.ui.components.GlassCard
import dev.gr1ff3n.mcnmt.ui.theme.AccentOrange
import dev.gr1ff3n.mcnmt.ui.theme.AccentOrangeBright
import dev.gr1ff3n.mcnmt.ui.theme.SleekText
import dev.gr1ff3n.mcnmt.ui.theme.SleekTextDim
import dev.gr1ff3n.mcnmt.ui.theme.appBackground
import java.time.Duration
import java.time.Instant
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

private val requiredPermissions: List<String> = buildList {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        add(Manifest.permission.POST_NOTIFICATIONS)
    }
    add(Manifest.permission.ACCESS_FINE_LOCATION)
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onViewTrips: () -> Unit = {},
    onOpenReports: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val activeTrip by viewModel.activeTrip.collectAsStateWithLifecycle()
    val monthReport by viewModel.monthReport.collectAsStateWithLifecycle()

    var permissionsGranted by remember {
        mutableStateOf(
            requiredPermissions.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        )
    }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { results -> permissionsGranted = results.values.all { it } }

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.currentStateFlow.collect { st ->
            if (st.isAtLeast(Lifecycle.State.RESUMED)) {
                permissionsGranted = requiredPermissions.all {
                    ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                }
            }
        }
    }

    Box(modifier = modifier.appBackground()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            HeaderBar(onOpenSettings = onOpenSettings)

            if (!permissionsGranted) {
                PermissionBanner(onGrant = { permLauncher.launch(requiredPermissions.toTypedArray()) })
            }

            HeroCard(
                activeTrip = activeTrip,
                enabled = permissionsGranted,
                onStart = viewModel::startManualTrip,
                onStop = viewModel::stopManualTrip,
            )

            MonthCard(report = monthReport)

            NavRow(
                onViewTrips = onViewTrips,
                onOpenReports = onOpenReports,
                onOpenSettings = onOpenSettings,
            )
        }
    }
}

@Composable
private fun HeaderBar(onOpenSettings: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.logo_mcn_circle),
            contentDescription = null,
            modifier = Modifier.size(30.dp),
        )
        Spacer(Modifier.size(10.dp))
        Text(
            "MCN Mileage",
            style = MaterialTheme.typography.titleLarge,
            color = SleekText,
        )
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x0DFFFFFF))
                .border(1.dp, Color(0x16FFFFFF), RoundedCornerShape(12.dp))
                .clickable(onClick = onOpenSettings),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = SleekTextDim, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun HeroCard(
    activeTrip: Trip?,
    enabled: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
) {
    GlassCard(strong = true, contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 30.dp, horizontal = 20.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = if (activeTrip == null) "NO ACTIVE TRIP" else "TRACKING",
                style = MaterialTheme.typography.labelMedium,
                color = if (activeTrip == null) SleekTextDim else AccentOrangeBright,
                letterSpacing = 1.5.sp,
            )

            if (activeTrip == null) {
                PulseButton(
                    enabled = enabled,
                    onClick = onStart,
                    ringColor = AccentOrange,
                    fill = Brush.radialGradient(
                        0.0f to AccentOrangeBright,
                        0.6f to AccentOrange,
                        1.0f to Color(0xFFD96400),
                    ),
                    icon = Icons.Filled.PlayArrow,
                    label = "START",
                    sub = "trip",
                )
                Text(
                    "Tap to begin tracking your drive",
                    style = MaterialTheme.typography.bodySmall,
                    color = SleekTextDim,
                )
            } else {
                LiveStats(activeTrip)
                PulseButton(
                    enabled = true,
                    onClick = onStop,
                    ringColor = Color(0xFFFF5A3C),
                    fill = Brush.radialGradient(
                        0.0f to Color(0xFFFF7A5C),
                        0.6f to Color(0xFFE23B27),
                        1.0f to Color(0xFFB52414),
                    ),
                    icon = Icons.Filled.Stop,
                    label = "STOP",
                    sub = null,
                )
            }
        }
    }
}

@Composable
private fun LiveStats(trip: Trip) {
    val now by produceState(initialValue = Instant.now()) {
        while (true) { value = Instant.now(); delay(1000) }
    }
    val miles = (trip.distanceCorrectionMeters ?: trip.distanceMeters) / 1609.344
    Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
        Stat("%.1f".format(miles), "miles", accent = true)
        Stat(formatDuration(Duration.between(trip.startTimeUtc, now)), "elapsed")
    }
}

@Composable
private fun PulseButton(
    enabled: Boolean,
    onClick: () -> Unit,
    ringColor: Color,
    fill: Brush,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    sub: String?,
) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "pulse",
    )
    Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
        // Breathing outer ring
        Box(
            modifier = Modifier
                .size(200.dp)
                .graphicsLayer {
                    val s = 0.78f + pulse * 0.22f
                    scaleX = s; scaleY = s; alpha = (1f - pulse) * 0.5f
                }
                .clip(CircleShape)
                .border(1.5.dp, ringColor, CircleShape),
        )
        Box(modifier = Modifier.size(166.dp).clip(CircleShape).border(1.dp, ringColor.copy(alpha = 0.35f), CircleShape))
        // The button
        Box(
            modifier = Modifier
                .size(140.dp)
                .shadow(28.dp, CircleShape, ambientColor = ringColor, spotColor = ringColor)
                .clip(CircleShape)
                .background(fill)
                .clickable(enabled = enabled, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(38.dp))
                Text(label, color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                if (sub != null) Text(sub, color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun MonthCard(report: MileageReport) {
    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("This month", style = MaterialTheme.typography.titleMedium, color = SleekText)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0x1F9FD0BF))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        report.period.format(PERIOD_FMT).uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF9FD0BF),
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Stat(report.rows.size.toString(), "Trips")
                Stat(report.totalMiles.toString(), "Miles")
                Stat("$%.0f".format(report.totalAmount), "Reimbursement", accent = true)
            }
        }
    }
}

@Composable
private fun Stat(value: String, label: String, accent: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            color = if (accent) AccentOrangeBright else SleekText,
        )
        Text(label, style = MaterialTheme.typography.labelMedium, color = SleekTextDim)
    }
}

@Composable
private fun NavRow(
    onViewTrips: () -> Unit,
    onOpenReports: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        NavTile("Trips", Icons.Filled.DirectionsCar, AccentOrange, Modifier.weight(1f), onViewTrips)
        NavTile("Report", Icons.Filled.Description, Color(0xFF9FD6E6), Modifier.weight(1f), onOpenReports)
        NavTile("Settings", Icons.Filled.Settings, Color(0xFFB9C8CE), Modifier.weight(1f), onOpenSettings)
    }
}

@Composable
private fun NavTile(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    GlassCard(
        modifier = modifier.clickable(onClick = onClick),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 18.dp, horizontal = 8.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(tint.copy(alpha = 0.12f))
                    .border(1.dp, tint.copy(alpha = 0.25f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(20.dp))
            }
            Text(label, style = MaterialTheme.typography.bodyMedium, color = SleekText)
        }
    }
}

@Composable
private fun PermissionBanner(onGrant: () -> Unit) {
    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Text("Permissions needed", style = MaterialTheme.typography.titleSmall, color = SleekText, fontWeight = FontWeight.SemiBold)
            Text(
                "MCN Mileage needs location and notification access to record trips.",
                style = MaterialTheme.typography.bodySmall,
                color = SleekTextDim,
                textAlign = TextAlign.Start,
            )
            Button(onClick = onGrant) { Text("Grant access") }
        }
    }
}

private fun formatDuration(d: Duration): String {
    val total = d.toMinutes().coerceAtLeast(0)
    val h = total / 60
    val m = total % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}

private val PERIOD_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
