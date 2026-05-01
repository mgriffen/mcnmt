package dev.gr1ff3n.mcnmt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.gr1ff3n.mcnmt.ui.nav.MileageNavGraph
import dev.gr1ff3n.mcnmt.ui.theme.MCNMTTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { MileageRoot() }
    }
}

@Composable
private fun MileageRoot() {
    MCNMTTheme {
        val nav = rememberNavController()
        Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
            MileageNavGraph(
                navController = nav,
                modifier = Modifier.padding(padding),
            )
        }
    }
}
