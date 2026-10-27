package com.rideshare.malawi.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rideshare.malawi.data.AppState
import com.rideshare.malawi.ui.screens.*

@Composable
fun RideShareNavHost(navController: NavHostController = rememberNavController()) {

    fun goHome(route: String) {
        navController.navigate(route) {
            popUpTo(Routes.LOGIN) { inclusive = false }
            launchSingleTop = true
        }
    }

    NavHost(navController = navController, startDestination = Routes.LOGIN) {

        composable(Routes.LOGIN) {
            LoginScreen(onLoggedIn = { navController.navigate(Routes.ROLE_SELECT) })
        }

        composable(Routes.ROLE_SELECT) {
            RoleSelectionScreen(
                onChooseDriver = { navController.navigate(Routes.DRIVER_VERIFICATION) },
                onChoosePassenger = { navController.navigate(Routes.PASSENGER_HOME) }
            )
        }

        composable(Routes.DRIVER_VERIFICATION) {
            DriverVerificationScreen(
                onBack = { navController.popBackStack() },
                onContinueToHome = { navController.navigate(Routes.DRIVER_HOME) },
                onHome = { goHome(Routes.DRIVER_HOME) }
            )
        }

        composable(Routes.DRIVER_HOME) {
            DriverHomeScreen(
                onCreateRide = { navController.navigate(Routes.CREATE_RIDE) },
                onOpenRequests = { navController.navigate(Routes.RIDE_REQUESTS) },
                onOpenRide = { }
            )
        }

        composable(Routes.CREATE_RIDE) {
            CreateRideScreen(
                onBack = { navController.popBackStack() },
                onRideCreated = { navController.popBackStack() },
                onHome = { goHome(Routes.DRIVER_HOME) }
            )
        }

        composable(Routes.RIDE_REQUESTS) {
            RideRequestsScreen(
                onBack = { navController.popBackStack() },
                onHome = { goHome(Routes.DRIVER_HOME) }
            )
        }

        composable(Routes.PASSENGER_HOME) {
            PassengerHomeScreen(
                onOpenRide = { rideId -> navController.navigate(Routes.rideDetails(rideId)) },
                onOpenRequest = { requestId -> navController.navigate(Routes.requestPayment(requestId)) }
            )
        }

        composable(
            route = Routes.RIDE_DETAILS,
            arguments = listOf(navArgument("rideId") { type = NavType.StringType })
        ) { backStackEntry ->
            val rideId = backStackEntry.arguments?.getString("rideId")
            val ride = AppState.rides.firstOrNull { it.id == rideId }
            if (ride != null) {
                RideDetailsScreen(
                    ride = ride,
                    onBack = { navController.popBackStack() },
                    onRequestSeat = {
                        navController.popBackStack(Routes.PASSENGER_HOME, inclusive = false)
                    },
                    onHome = { goHome(Routes.PASSENGER_HOME) }
                )
            }
        }

        composable(
            route = Routes.REQUEST_PAYMENT,
            arguments = listOf(navArgument("requestId") { type = NavType.StringType })
        ) { backStackEntry ->
            val requestId = backStackEntry.arguments?.getString("requestId") ?: ""
            RequestPaymentScreen(
                requestId = requestId,
                onBack = { navController.popBackStack() },
                onHome = { goHome(Routes.PASSENGER_HOME) }
            )
        }
    }
}
