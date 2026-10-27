package com.rideshare.malawi.v2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rideshare.malawi.v2.ui.components.ErrorText
import com.rideshare.malawi.v2.ui.components.PrimaryButton
import com.rideshare.malawi.v2.ui.components.ScreenScaffold
import com.rideshare.malawi.v2.viewmodel.RidesViewModel

@Composable
fun CreateRideScreen(
    ridesVm: RidesViewModel,
    onBack: () -> Unit,
    onCreated: () -> Unit
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

    ScreenScaffold(
        title = "Offer a ride",
        onBack = onBack
    ) {
        ridesVm.error?.let { err ->
            ErrorText(message = err)
            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = pickup,
            onValueChange = {
                pickup = it
                ridesVm.clearError()
            },
            label = { Text("Pickup location") },
            placeholder = { Text("e.g. Lilongwe") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = destination,
            onValueChange = {
                destination = it
                ridesVm.clearError()
            },
            label = { Text("Destination") },
            placeholder = { Text("e.g. Blantyre") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = date,
            onValueChange = {
                date = it
                ridesVm.clearError()
            },
            label = { Text("Date") },
            placeholder = { Text("e.g. Sat, 24 Oct") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = time,
            onValueChange = {
                time = it
                ridesVm.clearError()
            },
            label = { Text("Time") },
            placeholder = { Text("e.g. 06:30") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = seatsText,
            onValueChange = {
                seatsText = it.filter { ch -> ch.isDigit() }
                ridesVm.clearError()
            },
            label = { Text("Seats available") },
            placeholder = { Text("e.g. 3") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = priceText,
            onValueChange = {
                priceText = it.filter { ch -> ch.isDigit() }
                ridesVm.clearError()
            },
            label = { Text("Price per seat (MWK)") },
            placeholder = { Text("e.g. 18000") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(28.dp))

        PrimaryButton(
            text = "Publish ride",
            enabled = canPublish,
            onClick = {
                val seats = seatsText.toIntOrNull() ?: 0
                val price = priceText.toIntOrNull() ?: 0
                ridesVm.createRide(pickup, destination, date, time, seats, price) {
                    onCreated()
                }
            }
        )
    }
}
