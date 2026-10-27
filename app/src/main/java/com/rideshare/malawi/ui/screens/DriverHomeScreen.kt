package com.rideshare.malawi.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rideshare.malawi.data.AppState
import com.rideshare.malawi.data.RequestStatus
import com.rideshare.malawi.ui.components.PillTone
import com.rideshare.malawi.ui.components.RideCard
import com.rideshare.malawi.ui.components.StatusPill

@Composable
fun DriverHomeScreen(
    onCreateRide: () -> Unit,
    onOpenRequests: () -> Unit,
    onOpenRide: (String) -> Unit
) {
    val pendingRequests = AppState.incomingRequests.count { it.status == RequestStatus.PENDING }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateRide,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Create ride")
            }
        }
    ) { innerPadding ->
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
                Text(
                    "Driver Dashboard",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Welcome back, ${AppState.currentUserName.value.ifBlank { "Driver" }}.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(24.dp))

                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenRequests)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Ride requests", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                if (pendingRequests == 0) "No pending requests" else "$pendingRequests awaiting your response",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (pendingRequests > 0) {
                            StatusPill("$pendingRequests pending", PillTone.Pending)
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                Text(
                    "Rides you offered",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(12.dp))

                if (AppState.myCreatedRides.isEmpty()) {
                    Text(
                        "You haven't posted any rides yet. Tap + to offer a ride.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    AppState.myCreatedRides.forEach { ride ->
                        RideCard(ride = ride) { onOpenRide(ride.id) }
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}
