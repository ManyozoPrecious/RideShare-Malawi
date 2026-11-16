package com.rideshare.malawi.v2.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import com.rideshare.malawi.v2.ui.components.EmptyState
import com.rideshare.malawi.v2.ui.components.StatusPill
import com.rideshare.malawi.v2.ui.components.TabHeader
import com.rideshare.malawi.v2.ui.components.toPill
import com.rideshare.malawi.v2.ui.util.formatMwk
import com.rideshare.malawi.v2.viewmodel.AuthViewModel
import com.rideshare.malawi.v2.viewmodel.RidesViewModel

@Composable
fun BookingsScreen(
    authVm: AuthViewModel,
    ridesVm: RidesViewModel,
    onOpenBooking: (String) -> Unit
) {
    val requests by ridesVm.requests.collectAsState()
    val rides by ridesVm.rides.collectAsState()
    val users by ridesVm.users.collectAsState()
    val currentUser by authVm.currentUser.collectAsState()

    val myRequests = requests.filter { it.passengerId == currentUser?.id }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        TabHeader(title = "Bookings")

        if (myRequests.isEmpty()) {
            EmptyState(message = "You haven't requested a seat yet. Find a ride in the Find tab.")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(myRequests, key = { it.id }) { req ->
                    val ride = rides.find { it.id == req.rideId }
                    val driver = users.find { it.id == ride?.driverId }
                    val pill = req.status.toPill()

                    Card(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .testTag("booking_item_${req.id}")
                            .clickable { onOpenBooking(req.id) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (ride != null) "${ride.pickup} → ${ride.destination}" else "Ride details",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                StatusPill(label = pill.label, tone = pill.tone)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            if (ride != null) {
                                Text(
                                    text = "${ride.date} · ${ride.time} · ${formatMwk(ride.pricePerSeat)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Driver: ${driver?.name ?: "Unknown"}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
