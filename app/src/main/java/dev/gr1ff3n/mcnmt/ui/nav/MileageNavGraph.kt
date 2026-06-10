package dev.gr1ff3n.mcnmt.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import dev.gr1ff3n.mcnmt.ui.home.HomeScreen
import dev.gr1ff3n.mcnmt.ui.report.ReportScreen
import dev.gr1ff3n.mcnmt.ui.settings.SettingsScreen
import dev.gr1ff3n.mcnmt.ui.trips.TripDetailScreen
import dev.gr1ff3n.mcnmt.ui.trips.TripListScreen

object Routes {
    const val HOME = "home"
    const val TRIPS = "trips"
    const val NEW_TRIP = "trips/new"
    const val TRIP_DETAIL = "trips/{tripId}"
    const val SETTINGS = "settings"
    const val REPORTS = "reports"

    fun tripDetail(tripId: Long): String = "trips/$tripId"
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
        composable(Routes.HOME) {
            HomeScreen(
                onViewTrips = { navController.navigate(Routes.TRIPS) },
                onOpenReports = { navController.navigate(Routes.REPORTS) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }
        composable(Routes.REPORTS) {
            ReportScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.TRIPS) {
            TripListScreen(
                onBack = { navController.popBackStack() },
                onTripClick = { tripId -> navController.navigate(Routes.tripDetail(tripId)) },
                onAddTrip = { navController.navigate(Routes.NEW_TRIP) },
            )
        }
        // Literal "trips/new" must be registered before the "trips/{tripId}"
        // pattern so the manual-entry route wins the match.
        composable(Routes.NEW_TRIP) {
            TripDetailScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Routes.TRIP_DETAIL,
            arguments = listOf(navArgument("tripId") { type = NavType.LongType }),
        ) {
            TripDetailScreen(onBack = { navController.popBackStack() })
        }
    }
}
