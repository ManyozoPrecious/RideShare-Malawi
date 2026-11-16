package com.rideshare.malawi.v2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rideshare.malawi.v2.data.PaymentStatus
import com.rideshare.malawi.v2.data.RequestStatus
import com.rideshare.malawi.v2.data.paychangu.MobileMoneyOperator
import com.rideshare.malawi.v2.ui.components.ScreenScaffold
import com.rideshare.malawi.v2.ui.components.StatusPill
import com.rideshare.malawi.v2.ui.components.toPill
import com.rideshare.malawi.v2.ui.util.displayPhone
import com.rideshare.malawi.v2.ui.util.formatMwk
import com.rideshare.malawi.v2.ui.util.openDialer
import com.rideshare.malawi.v2.ui.util.openSms
import com.rideshare.malawi.v2.viewmodel.RidesViewModel

@Composable
fun BookingDetailsScreen(
    requestId: String,
    ridesVm: RidesViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val requests by ridesVm.requests.collectAsState()
    val rides by ridesVm.rides.collectAsState()
    val users by ridesVm.users.collectAsState()

    val request = requests.find { it.id == requestId }
    val ride = rides.find { it.id == request?.rideId }
    val driver = users.find { it.id == ride?.driverId }

    var selectedOperator by remember { mutableStateOf(MobileMoneyOperator.AIRTEL_MONEY) }
    var mobileMoneyPhone by remember { mutableStateOf("") }

    LaunchedEffect(driver) {
        if (mobileMoneyPhone.isBlank()) {
            mobileMoneyPhone = "0991234567"
        }
    }

    ScreenScaffold(
        title = "Booking details",
        onBack = onBack
    ) {
        if (request == null) {
            Text(
                text = "Booking not found.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return@ScreenScaffold
        }

        val pill = request.status.toPill()

        Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (ride != null) "${ride.pickup} → ${ride.destination}" else "Ride",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusPill(label = pill.label, tone = pill.tone)
                }

                if (ride != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${ride.date} · Departs ${ride.time}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatMwk(ride.pricePerSeat),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        when (request.status) {
            RequestStatus.PENDING -> {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Waiting for ${driver?.name ?: "the driver"} to respond.",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "You'll see driver contact details and PayChangu payment option here once they accept your request.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            RequestStatus.REJECTED -> {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "This driver declined your request.",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try another ride on the same route from the Find tab.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            RequestStatus.ACCEPTED -> {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Your seat is confirmed",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // PayChangu Payment Card
                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "PayChangu Mobile Money",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        when (request.paymentStatus) {
                            PaymentStatus.UNPAID, PaymentStatus.FAILED -> {
                                if (request.paymentStatus == PaymentStatus.FAILED) {
                                    Text(
                                        text = "Previous payment attempt failed. Please try again.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                Text(
                                    text = "Select Mobile Money Operator:",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    FilterChip(
                                        selected = selectedOperator == MobileMoneyOperator.AIRTEL_MONEY,
                                        onClick = { selectedOperator = MobileMoneyOperator.AIRTEL_MONEY },
                                        label = { Text("Airtel Money") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    FilterChip(
                                        selected = selectedOperator == MobileMoneyOperator.TNM_MPAMBA,
                                        onClick = { selectedOperator = MobileMoneyOperator.TNM_MPAMBA },
                                        label = { Text("TNM Mpamba") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = mobileMoneyPhone,
                                    onValueChange = { mobileMoneyPhone = it },
                                    label = { Text("Mobile Money Phone Number") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        if (ride != null) {
                                            ridesVm.processPayChanguPayment(
                                                requestId = request.id,
                                                operator = selectedOperator,
                                                phone = mobileMoneyPhone,
                                                amount = ride.pricePerSeat
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Pay ${formatMwk(ride?.pricePerSeat ?: 0)} with PayChangu")
                                }
                            }

                            PaymentStatus.PROCESSING -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                                ) {
                                    CircularProgressIndicator()
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Sending PayChangu PIN prompt to $mobileMoneyPhone…",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }

                            PaymentStatus.SUCCESS -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "${formatMwk(ride?.pricePerSeat ?: 0)} Paid via PayChangu",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (request.paymentStatus == PaymentStatus.SUCCESS) {
                    Card(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Driver contact",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = driver?.name ?: "Driver",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            val phoneStr = driver?.phone ?: ""
                            Text(
                                text = displayPhone(phoneStr),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = { context.openDialer(phoneStr) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = "Call"
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Call")
                                }

                                OutlinedButton(
                                    onClick = { context.openSms(phoneStr) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Sms,
                                        contentDescription = "SMS"
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("SMS")
                                }
                            }
                        }
                    }
                } else {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Complete payment above to unlock driver contact details (Call & SMS).",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
