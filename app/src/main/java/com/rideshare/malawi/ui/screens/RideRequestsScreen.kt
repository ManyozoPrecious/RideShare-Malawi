package com.rideshare.malawi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rideshare.malawi.data.AppState
import com.rideshare.malawi.data.RequestStatus
import com.rideshare.malawi.ui.components.PrimaryButton
import com.rideshare.malawi.ui.components.ScreenScaffold
import com.rideshare.malawi.ui.components.SecondaryButton
import com.rideshare.malawi.ui.components.StatusPill
import com.rideshare.malawi.ui.components.toPill

@Composable
fun RideRequestsScreen(
    onBack: () -> Unit,
    onHome: () -> Unit
) {
    ScreenScaffold(title = "Ride requests", onBack = onBack, onHome = onHome) {
        if (AppState.incomingRequests.isEmpty()) {
            Spacer(Modifier.height(24.dp))
            Text("No incoming requests yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            return@ScreenScaffold
        }

        AppState.incomingRequests.forEach { request ->
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            Text(request.passengerName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("${request.ride.pickup} → ${request.ride.destination} · ${request.ride.date}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        val (label, tone) = request.status.toPill()
                        StatusPill(label, tone)
                    }

                    if (request.status == RequestStatus.PENDING) {
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(Modifier.weight(1f)) {
                                SecondaryButton(text = "Decline") {
                                    AppState.updateRequestStatus(request, RequestStatus.REJECTED)
                                }
                            }
                            Box(Modifier.weight(1f)) {
                                PrimaryButton(text = "Accept") {
                                    AppState.updateRequestStatus(request, RequestStatus.ACCEPTED)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}
