package com.rideshare.malawi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rideshare.malawi.data.AppState
import com.rideshare.malawi.data.PaymentStatus
import com.rideshare.malawi.data.RequestStatus
import com.rideshare.malawi.data.RideRequest
import com.rideshare.malawi.ui.components.PrimaryButton
import com.rideshare.malawi.ui.components.ScreenScaffold
import com.rideshare.malawi.ui.components.SecondaryButton
import com.rideshare.malawi.ui.components.StatusPill
import com.rideshare.malawi.ui.components.toPill
import com.rideshare.malawi.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RequestPaymentScreen(requestId: String, onBack: () -> Unit, onHome: () -> Unit) {
    val request = AppState.myRequests.firstOrNull { it.id == requestId }
    val scope = rememberCoroutineScope()

    ScreenScaffold(title = "Your request", onBack = onBack, onHome = onHome) {
        if (request == null) {
            Spacer(Modifier.height(40.dp))
            Text("This request could not be found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            return@ScreenScaffold
        }

        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text(
                    "${request.ride.pickup} → ${request.ride.destination}",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    "${request.ride.date} · ${request.ride.time}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            val (label, tone) = request.status.toPill()
            StatusPill(label, tone)
        }
        Spacer(Modifier.height(24.dp))

        when (request.status) {
            RequestStatus.PENDING -> PendingBlock(request)
            RequestStatus.REJECTED -> RejectedBlock()
            RequestStatus.ACCEPTED -> AcceptedBlock(request, scope)
        }
    }
}

@Composable
private fun PendingBlock(request: RideRequest) {
    Text(
        "Waiting for ${request.ride.driver.name} to respond to your request.",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(Modifier.height(24.dp))
    Text(
        "Demo note: since this prototype runs on one device, use the button below to simulate the driver accepting your request.",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(10.dp))
    SecondaryButton(text = "Simulate driver acceptance") {
        AppState.updateRequestStatus(request, RequestStatus.ACCEPTED)
    }
}

@Composable
private fun RejectedBlock() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(top = 32.dp)) {
        Icon(Icons.Filled.Error, contentDescription = null, tint = ErrorBrick, modifier = Modifier.size(40.dp))
        Spacer(Modifier.height(12.dp))
        Text("This driver declined your request.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
        Text("Try searching for another ride on the same route.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AcceptedBlock(request: RideRequest, scope: CoroutineScope) {
    when (request.payment) {
        PaymentStatus.UNPAID, PaymentStatus.FAILED -> {
            if (request.payment == PaymentStatus.FAILED) {
                Text("Payment didn't go through. You haven't been charged.", color = ErrorBrick, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(16.dp))
            }
            Text("Your seat is confirmed. Complete payment to finish booking.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(16.dp))
            AmountCard(request)
            Spacer(Modifier.height(20.dp))
            PrimaryButton(text = "Pay with PayChangu (sandbox)") {
                AppState.updatePayment(request, PaymentStatus.PROCESSING)
                scope.launch {
                    delay(1600)
                    AppState.updatePayment(request, PaymentStatus.SUCCESS)
                }
            }
        }
        PaymentStatus.PROCESSING -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp)) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(16.dp))
                Text("Processing payment…", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
            }
        }
        PaymentStatus.SUCCESS -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(44.dp))
                Spacer(Modifier.height(10.dp))
                Text("Payment successful", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
                Text("MWK ${"%,d".format(request.ride.pricePerSeat)} paid", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(28.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.height(20.dp))
            Text("Driver contact", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = TealSoft),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Phone, contentDescription = null, tint = TealDeep)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(request.ride.driver.name, style = MaterialTheme.typography.titleMedium, color = TealDeep)
                        Text(request.ride.driver.phone, style = MaterialTheme.typography.bodyMedium, color = TealDeep)
                    }
                }
            }
        }
    }
}

@Composable
private fun AmountCard(request: RideRequest) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Amount due", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "MWK ${"%,d".format(request.ride.pricePerSeat)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
