package com.rideshare.malawi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rideshare.malawi.data.AppState
import com.rideshare.malawi.data.Driver
import com.rideshare.malawi.data.Ride
import com.rideshare.malawi.data.VerificationStatus
import com.rideshare.malawi.ui.components.PrimaryButton
import com.rideshare.malawi.ui.components.ScreenScaffold

@Composable
fun CreateRideScreen(
    onBack: () -> Unit,
    onRideCreated: () -> Unit,
    onHome: () -> Unit
) {
    var pickup by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var seatsText by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }

    val canPublish = pickup.isNotBlank() &&
            destination.isNotBlank() &&
            date.isNotBlank() &&
            time.isNotBlank() &&
            seatsText.isNotBlank() &&
            priceText.isNotBlank()

    ScreenScaffold(title = "Offer a ride", onBack = onBack, onHome = onHome) {
        OutlinedTextField(
            value = pickup,
            onValueChange = { pickup = it },
            label = { Text("Pickup location") },
            placeholder = { Text("e.g. Kasungu") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = destination,
            onValueChange = { destination = it },
            label = { Text("Destination") },
            placeholder = { Text("e.g. Lilongwe") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Date") },
            placeholder = { Text("e.g. Mon, 13 Oct") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = time,
            onValueChange = { time = it },
            label = { Text("Time") },
            placeholder = { Text("e.g. 07:30") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = seatsText,
            onValueChange = { seatsText = it },
            label = { Text("Seats available") },
            placeholder = { Text("e.g. 3") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = priceText,
            onValueChange = { priceText = it },
            label = { Text("Price per seat (MWK)") },
            placeholder = { Text("e.g. 8000") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(28.dp))

        PrimaryButton(
            text = "Publish ride",
            enabled = canPublish
        ) {
            val driver = Driver(
                id = "d-self",
                name = AppState.currentUserName.value.ifBlank { "You" },
                phone = "+265 ...",
                vehicleRegNumber = "BT 4521",
                verification = VerificationStatus.VERIFIED,
                rating = 4.8f
            )
            val newRide = Ride(
                id = "r-${System.currentTimeMillis()}",
                driver = driver,
                pickup = pickup.trim(),
                destination = destination.trim(),
                date = date.trim(),
                time = time.trim(),
                seatsAvailable = seatsText.trim().toIntOrNull() ?: 1,
                pricePerSeat = priceText.trim().toIntOrNull() ?: 0
            )
            AppState.addRide(newRide)
            onRideCreated()
        }
    }
}
