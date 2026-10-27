package com.rideshare.malawi.v2.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rideshare.malawi.v2.data.UserRole
import com.rideshare.malawi.v2.ui.screens.*
import com.rideshare.malawi.v2.viewmodel.AuthViewModel
import com.rideshare.malawi.v2.viewmodel.RidesViewModel

private data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun RideShareNavHost(
    navController: NavHostController = rememberNavController()
) {
    val authVm: AuthViewModel = viewModel { AuthViewModel() }
    val ridesVm: RidesViewModel = viewModel { RidesViewModel() }

    val currentUser by authVm.currentUser.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isDriver = currentUser?.role == UserRole.DRIVER

    val driverTabs = listOf(
        NavItem(Routes.DRIVER_RIDES, "My rides", Icons.Default.DirectionsCar),
        NavItem(Routes.DRIVER_REQUESTS, "Requests", Icons.AutoMirrored.Filled.ListAlt),
        NavItem(Routes.PROFILE, "Profile", Icons.Default.AccountCircle)
    )

    val passengerTabs = listOf(
        NavItem(Routes.PASSENGER_SEARCH, "Find", Icons.Default.Search),
        NavItem(Routes.PASSENGER_BOOKINGS, "Bookings", Icons.Default.ConfirmationNumber),
        NavItem(Routes.PROFILE, "Profile", Icons.Default.AccountCircle)
    )

    val currentTabs = if (isDriver) driverTabs else passengerTabs
    val tabRoutes = setOf(
        Routes.DRIVER_RIDES,
        Routes.DRIVER_REQUESTS,
        Routes.PASSENGER_SEARCH,
        Routes.PASSENGER_BOOKINGS,
        Routes.PROFILE
    )

    val showBottomBar = currentRoute in tabRoutes

    fun navigateAfterAuth(role: UserRole) {
        val targetTab = if (role == UserRole.DRIVER) Routes.DRIVER_RIDES else Routes.PASSENGER_SEARCH
        navController.navigate(targetTab) {
            popUpTo(Routes.LOGIN) { inclusive = true }
            launchSingleTop = true
        }
    }

    fun handleLogout() {
        navController.navigate(Routes.LOGIN) {
            popUpTo(0) { inclusive = true }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    currentTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                if (currentRoute != tab.route) {
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            label = { Text(tab.label) },
                            icon = { Icon(imageVector = tab.icon, contentDescription = tab.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.LOGIN,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    authVm = authVm,
                    onLoggedIn = { role -> navigateAfterAuth(role) },
                    onRegister = { navController.navigate(Routes.REGISTER) }
                )
            }

            composable(Routes.REGISTER) {
                RegisterScreen(
                    authVm = authVm,
                    onBack = { navController.popBackStack() },
                    onRegistered = { role -> navigateAfterAuth(role) }
                )
            }

            composable(Routes.DRIVER_RIDES) {
                DriverRidesScreen(
                    authVm = authVm,
                    ridesVm = ridesVm,
                    onCreateRide = { navController.navigate(Routes.CREATE_RIDE) },
                    onOpenRide = { rideId -> navController.navigate(Routes.rideDetails(rideId)) },
                    onVerify = { navController.navigate(Routes.DRIVER_VERIFICATION) }
                )
            }

            composable(Routes.DRIVER_REQUESTS) {
                DriverRequestsScreen(
                    authVm = authVm,
                    ridesVm = ridesVm
                )
            }

            composable(Routes.PASSENGER_SEARCH) {
                PassengerSearchScreen(
                    ridesVm = ridesVm,
                    onOpenRide = { rideId -> navController.navigate(Routes.rideDetails(rideId)) }
                )
            }

            composable(Routes.PASSENGER_BOOKINGS) {
                BookingsScreen(
                    authVm = authVm,
                    ridesVm = ridesVm,
                    onOpenBooking = { requestId -> navController.navigate(Routes.bookingDetails(requestId)) }
                )
            }

            composable(Routes.PROFILE) {
                ProfileScreen(
                    authVm = authVm,
                    onLogout = { handleLogout() },
                    onOpenVerification = { navController.navigate(Routes.DRIVER_VERIFICATION) }
                )
            }

            composable(Routes.DRIVER_VERIFICATION) {
                DriverVerificationScreen(
                    authVm = authVm,
                    onBack = { navController.popBackStack() },
                    onDone = { navController.popBackStack() }
                )
            }

            composable(Routes.CREATE_RIDE) {
                CreateRideScreen(
                    ridesVm = ridesVm,
                    onBack = { navController.popBackStack() },
                    onCreated = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.RIDE_DETAILS,
                arguments = listOf(navArgument("rideId") { type = NavType.StringType })
            ) { backStackEntry ->
                val rideId = backStackEntry.arguments?.getString("rideId") ?: ""
                RideDetailsScreen(
                    rideId = rideId,
                    authVm = authVm,
                    ridesVm = ridesVm,
                    onBack = { navController.popBackStack() },
                    onRequested = { navController.popBackStack(Routes.PASSENGER_SEARCH, inclusive = false) },
                    onOpenBooking = { reqId -> navController.navigate(Routes.bookingDetails(reqId)) }
                )
            }

            composable(
                route = Routes.BOOKING_DETAILS,
                arguments = listOf(navArgument("requestId") { type = NavType.StringType })
            ) { backStackEntry ->
                val requestId = backStackEntry.arguments?.getString("requestId") ?: ""
                BookingDetailsScreen(
                    requestId = requestId,
                    ridesVm = ridesVm,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
