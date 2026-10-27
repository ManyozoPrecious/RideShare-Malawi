package com.rideshare.malawi.v2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import com.rideshare.malawi.v2.data.RequestStatus
import com.rideshare.malawi.v2.ui.components.EmptyState
import com.rideshare.malawi.v2.ui.components.ErrorText
import com.rideshare.malawi.v2.ui.components.PrimaryButton
import com.rideshare.malawi.v2.ui.components.SecondaryButton
import com.rideshare.malawi.v2.ui.components.StatusPill
import com.rideshare.malawi.v2.ui.components.TabHeader
import com.rideshare.malawi.v2.ui.components.toPill
import com.rideshare.malawi.v2.viewmodel.AuthViewModel
import com.rideshare.malawi.v2.viewmodel.RidesViewModel

@Composable
fun DriverRequestsScreen(
    authVm: AuthViewModel,
    ridesVm: RidesViewModel
) {
    val currentUser by authVm.currentUser.collectAsState()
    val requests by ridesVm.requests.collectAsState()
    val rides by ridesVm.rides.collectAsState()
    val users by ridesVm.users.collectAsState()

    val myRideIds = rides.filter { it.driverId == currentUser?.id }.map { it.id }.toSet()
    val myRequests = requests.filter { it.rideId in myRideIds }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        TabHeader(title = "Requests")

        ridesVm.error?.let { err ->
            ErrorText(message = err)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (myRequests.isEmpty()) {
            EmptyState(message = "Requests from passengers will show up here once someone asks for a seat.")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(myRequests, key = { it.id }) { req ->
                    val ride = rides.find { it.id == req.rideId }
                    val passenger = users.find { it.id == req.passengerId }
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
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = passenger?.name ?: "Passenger",
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
                                    text = "${ride.pickup} → ${ride.destination}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${ride.date} · ${ride.time}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (req.status == RequestStatus.PENDING) {
                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    SecondaryButton(
                                        text = "Decline",
                                        onClick = { ridesVm.respond(req.id, accept = false) },
                                        modifier = Modifier.weight(1f)
                                    )

                                    PrimaryButton(
                                        text = "Accept",
                                        onClick = { ridesVm.respond(req.id, accept = true) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
