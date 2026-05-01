package dev.gr1ff3n.mcnmt.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.gr1ff3n.mcnmt.R
import dev.gr1ff3n.mcnmt.ui.theme.MCNMTTheme

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val tripCount by viewModel.tripCount.collectAsStateWithLifecycle()
    var tracking by remember { mutableStateOf(false) }
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
                text = stringResource(R.string.home_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = if (tracking) stringResource(R.string.notif_tracking_text)
                       else stringResource(R.string.home_no_active_trip),
                style = MaterialTheme.typography.bodyLarge,
                color = if (tracking) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (tracking) FontWeight.SemiBold else FontWeight.Normal,
            )
            Button(
                onClick = { tracking = !tracking },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (tracking) MaterialTheme.colorScheme.secondary
                                     else MaterialTheme.colorScheme.primary,
                    contentColor = if (tracking) MaterialTheme.colorScheme.onSecondary
                                   else MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text(
                    text = if (tracking) stringResource(R.string.home_stop_trip)
                           else stringResource(R.string.home_start_trip),
                )
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

@Preview
@Composable
private fun HomeScreenPreview() {
    MCNMTTheme { HomeScreen() }
}
