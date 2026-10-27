package com.rideshare.malawi.v2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import com.rideshare.malawi.v2.ui.components.EmptyState
import com.rideshare.malawi.v2.ui.components.RideCard
import com.rideshare.malawi.v2.ui.components.SectionHeading
import com.rideshare.malawi.v2.ui.components.TabHeader
import com.rideshare.malawi.v2.viewmodel.RidesViewModel

@Composable
fun PassengerSearchScreen(
    ridesVm: RidesViewModel,
    onOpenRide: (String) -> Unit
) {
    val rides by ridesVm.rides.collectAsState()
    val users by ridesVm.users.collectAsState()

    var fromQuery by remember { mutableStateOf("") }
    var toQuery by remember { mutableStateOf("") }
    var dateQuery by remember { mutableStateOf("") }

    val filteredRides = remember(rides, fromQuery, toQuery, dateQuery) {
        rides.filter { ride ->
            (fromQuery.isBlank() || ride.pickup.contains(fromQuery.trim(), ignoreCase = true)) &&
            (toQuery.isBlank() || ride.destination.contains(toQuery.trim(), ignoreCase = true)) &&
            (dateQuery.isBlank() || ride.date.contains(dateQuery.trim(), ignoreCase = true))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        TabHeader(title = "Find a ride")

        ridesVm.notice?.let { notice ->
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = notice,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { ridesVm.clearNotice() }) {
                        Text(
                            text = "OK",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = fromQuery,
            onValueChange = { fromQuery = it },
            label = { Text("From") },
            placeholder = { Text("e.g. Lilongwe") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = toQuery,
            onValueChange = { toQuery = it },
            label = { Text("To") },
            placeholder = { Text("e.g. Blantyre") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = dateQuery,
            onValueChange = { dateQuery = it },
            label = { Text("Date (optional)") },
            placeholder = { Text("e.g. Sat, 24 Oct") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        SectionHeading(title = "${filteredRides.size} ride(s) found")

        if (filteredRides.isEmpty()) {
            EmptyState(message = "No rides match that search yet. Try different towns or clear the date.")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredRides, key = { it.id }) { ride ->
                    val driver = users.find { it.id == ride.driverId }
                    RideCard(
                        ride = ride,
                        driver = driver,
                        onClick = { onOpenRide(ride.id) }
                    )
                }
            }
        }
    }
}
