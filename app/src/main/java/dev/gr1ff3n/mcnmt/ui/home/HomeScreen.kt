package dev.gr1ff3n.mcnmt.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.gr1ff3n.mcnmt.R
import dev.gr1ff3n.mcnmt.ui.theme.MCNMTTheme

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    var tracking by remember { mutableStateOf(false) }
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.home_title),
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = if (tracking) stringResource(R.string.notif_tracking_text)
                       else stringResource(R.string.home_no_active_trip),
                style = MaterialTheme.typography.bodyLarge,
            )
            Button(onClick = { tracking = !tracking }) {
                Text(
                    if (tracking) stringResource(R.string.home_stop_trip)
                    else stringResource(R.string.home_start_trip)
                )
            }
        }
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    MCNMTTheme { HomeScreen() }
}
