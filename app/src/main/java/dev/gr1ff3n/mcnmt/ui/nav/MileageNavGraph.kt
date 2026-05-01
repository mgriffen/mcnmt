package dev.gr1ff3n.mcnmt.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dev.gr1ff3n.mcnmt.ui.home.HomeScreen

object Routes {
    const val HOME = "home"
    // const val TRIPS = "trips"
    // const val TRIP_DETAIL = "trip/{tripId}"
    // const val REPORTS = "reports"
    // const val SETTINGS = "settings"
}

@Composable
fun MileageNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier,
    ) {
        composable(Routes.HOME) { HomeScreen() }
    }
}
