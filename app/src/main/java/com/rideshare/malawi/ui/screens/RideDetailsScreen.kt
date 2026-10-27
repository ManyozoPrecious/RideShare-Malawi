package com.rideshare.malawi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rideshare.malawi.data.AppState
import com.rideshare.malawi.data.Ride
import com.rideshare.malawi.ui.components.PrimaryButton
import com.rideshare.malawi.ui.components.ScreenScaffold
import com.rideshare.malawi.ui.components.StatusPill
import com.rideshare.malawi.ui.components.toPill

@Composable
fun RideDetailsScreen(
    ride: Ride,
    onBack: () -> Unit,
    onRequestSeat: () -> Unit,
    onHome: () -> Unit
) {
    ScreenScaffold(title = "Ride details", onBack = onBack, onHome = onHome) {
        Text(
            "${ride.pickup} → ${ride.destination}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "${ride.date} · Departs ${ride.time}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(20.dp))

        Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${ride.driver.name} · ★ ${ride.driver.rating} rating",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    val (label, tone) = ride.driver.verification.toPill()
                    StatusPill(label, tone)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Vehicle: ${ride.driver.vehicleRegNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Available seats", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "${ride.seatsAvailable} seat(s) available",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Price per seat", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "MWK ${"%,d".format(ride.pricePerSeat)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(Modifier.height(32.dp))

        PrimaryButton(
            text = "Request a seat",
            enabled = ride.seatsAvailable > 0
        ) {
            val name = AppState.currentUserName.value.ifBlank { "Grace Mwale" }
            AppState.requestSeat(ride, name)
            onRequestSeat()
        }
    }
}
