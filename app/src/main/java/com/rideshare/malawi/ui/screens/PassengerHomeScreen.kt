package com.rideshare.malawi.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rideshare.malawi.data.AppState
import com.rideshare.malawi.data.RideRequest
import com.rideshare.malawi.ui.components.RideCard
import com.rideshare.malawi.ui.components.StatusPill
import com.rideshare.malawi.ui.components.toPill

@Composable
fun PassengerHomeScreen(
    onOpenRide: (String) -> Unit,
    onOpenRequest: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }

    val filteredRides = remember(query, AppState.rides.toList()) {
        val q = query.trim().lowercase()
        if (q.isBlank()) AppState.rides.toList()
        else AppState.rides.filter {
            it.pickup.lowercase().contains(q) || it.destination.lowercase().contains(q)
        }
    }

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
            Text(
                "Find a ride",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("e.g. Lilongwe") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small
            )

            Spacer(Modifier.height(20.dp))

            if (AppState.myRequests.isNotEmpty()) {
                Text("Your requests", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                AppState.myRequests.forEach { req ->
                    RequestSummaryCard(req) { onOpenRequest(req.id) }
                    Spacer(Modifier.height(8.dp))
                }
                Spacer(Modifier.height(16.dp))
            }

            Text(
                "${filteredRides.size} ride${if (filteredRides.size == 1) "" else "s"} available",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(12.dp))

            if (filteredRides.isEmpty()) {
                Text(
                    "No rides match that search yet. Try a different town.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                filteredRides.forEach { ride ->
                    RideCard(ride = ride) { onOpenRide(ride.id) }
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun RequestSummaryCard(request: RideRequest, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("${request.ride.pickup} → ${request.ride.destination}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text("Driver: ${request.ride.driver.name}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            val (label, tone) = request.status.toPill()
            StatusPill(label, tone)
        }
    }
}
