package dev.gr1ff3n.mcnmt.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import dev.gr1ff3n.mcnmt.ui.home.HomeScreen
import dev.gr1ff3n.mcnmt.ui.trips.TripDetailScreen
import dev.gr1ff3n.mcnmt.ui.trips.TripListScreen

object Routes {
    const val HOME = "home"
    const val TRIPS = "trips"
    const val TRIP_DETAIL = "trips/{tripId}"

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
            )
        }
        composable(Routes.TRIPS) {
            TripListScreen(
                onBack = { navController.popBackStack() },
                onTripClick = { tripId -> navController.navigate(Routes.tripDetail(tripId)) },
            )
        }
        composable(
            route = Routes.TRIP_DETAIL,
            arguments = listOf(navArgument("tripId") { type = NavType.LongType }),
        ) {
            TripDetailScreen(onBack = { navController.popBackStack() })
        }
    }
}
