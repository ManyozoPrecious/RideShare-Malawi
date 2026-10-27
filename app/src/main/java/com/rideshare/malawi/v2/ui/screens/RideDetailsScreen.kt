package com.rideshare.malawi.v2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import com.rideshare.malawi.v2.data.RequestStatus
import com.rideshare.malawi.v2.data.UserRole
import com.rideshare.malawi.v2.ui.components.ErrorText
import com.rideshare.malawi.v2.ui.components.PrimaryButton
import com.rideshare.malawi.v2.ui.components.ScreenScaffold
import com.rideshare.malawi.v2.ui.components.SecondaryButton
import com.rideshare.malawi.v2.ui.components.StatusPill
import com.rideshare.malawi.v2.ui.components.toPill
import com.rideshare.malawi.v2.ui.util.formatMwk
import com.rideshare.malawi.v2.viewmodel.AuthViewModel
import com.rideshare.malawi.v2.viewmodel.RidesViewModel

@Composable
fun RideDetailsScreen(
    rideId: String,
    authVm: AuthViewModel,
    ridesVm: RidesViewModel,
    onBack: () -> Unit,
    onRequested: () -> Unit,
    onOpenBooking: (String) -> Unit
) {
    val rides by ridesVm.rides.collectAsState()
    val users by ridesVm.users.collectAsState()
    val requests by ridesVm.requests.collectAsState()
    val currentUser by authVm.currentUser.collectAsState()

    val ride = rides.find { it.id == rideId }
    val driver = users.find { it.id == ride?.driverId }

    val activeRequest = requests.find {
        it.rideId == rideId && it.passengerId == currentUser?.id && it.status != RequestStatus.REJECTED
    }

    ScreenScaffold(
        title = "Ride details",
        onBack = onBack
    ) {
        if (ride == null) {
            Text(
                text = "This ride no longer exists.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return@ScreenScaffold
        }

        ridesVm.error?.let { err ->
            ErrorText(message = err)
            Spacer(modifier = Modifier.height(12.dp))
        }

        Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "${ride.pickup} → ${ride.destination}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${ride.date} · Departs ${ride.time}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val seatsText = if (ride.seatsAvailable > 0) {
                        "${ride.seatsAvailable} seat(s) available"
                    } else {
                        "Fully booked"
                    }
                    Text(
                        text = seatsText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (ride.seatsAvailable > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )

                    Text(
                        text = formatMwk(ride.pricePerSeat),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Driver info",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                val driverName = driver?.name ?: "Driver"
                val rating = driver?.rating ?: 4.7f
                Text(
                    text = driverName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "★ ${"%.1f".format(rating)} · ${driver?.vehicleModel ?: ""} (${driver?.vehicleReg ?: ""})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (driver != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val pill = driver.verification.toPill()
                    StatusPill(label = pill.label, tone = pill.tone)
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        when {
            currentUser?.role == UserRole.PASSENGER && activeRequest == null -> {
                PrimaryButton(
                    text = "Request a seat",
                    enabled = ride.seatsAvailable > 0,
                    onClick = {
                        ridesVm.requestSeat(rideId, driver?.name ?: "Driver") {
                            onRequested()
                        }
                    }
                )
            }

            currentUser?.role == UserRole.PASSENGER && activeRequest != null -> {
                val pill = activeRequest.status.toPill()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Booking status:",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    StatusPill(label = pill.label, tone = pill.tone)
                }

                Spacer(modifier = Modifier.height(16.dp))

                SecondaryButton(
                    text = "View booking",
                    onClick = { onOpenBooking(activeRequest.id) }
                )
            }

            currentUser?.id == ride.driverId -> {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "This is your ride.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            currentUser?.role == UserRole.DRIVER -> {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Log in as a passenger to book seats.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}
