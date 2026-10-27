package com.rideshare.malawi.v2.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import com.rideshare.malawi.v2.data.RequestStatus
import com.rideshare.malawi.v2.data.VerificationStatus
import com.rideshare.malawi.v2.ui.components.EmptyState
import com.rideshare.malawi.v2.ui.components.RideCard
import com.rideshare.malawi.v2.ui.components.TabHeader
import com.rideshare.malawi.v2.viewmodel.AuthViewModel
import com.rideshare.malawi.v2.viewmodel.RidesViewModel

@Composable
fun DriverRidesScreen(
    authVm: AuthViewModel,
    ridesVm: RidesViewModel,
    onCreateRide: () -> Unit,
    onOpenRide: (String) -> Unit,
    onVerify: () -> Unit
) {
    val currentUser by authVm.currentUser.collectAsState()
    val rides by ridesVm.rides.collectAsState()
    val requests by ridesVm.requests.collectAsState()

    val myRides = rides.filter { it.driverId == currentUser?.id }
    val isVerified = currentUser?.verification == VerificationStatus.VERIFIED

    Scaffold(
        floatingActionButton = {
            if (isVerified) {
                FloatingActionButton(
                    onClick = onCreateRide,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create ride"
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            TabHeader(title = "My rides")

            if (!isVerified) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clickable(onClick = onVerify)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "You are not verified yet. Tap here to complete driver verification and offer rides.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (myRides.isEmpty()) {
                EmptyState(message = "You haven't offered any rides yet. Tap + to offer a ride.")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(myRides, key = { it.id }) { ride ->
                        val pendingCount = requests.count {
                            it.rideId == ride.id && it.status == RequestStatus.PENDING
                        }
                        val noteText = if (pendingCount > 0) "$pendingCount pending request(s)" else null

                        RideCard(
                            ride = ride,
                            driver = currentUser,
                            note = noteText,
                            onClick = { onOpenRide(ride.id) }
                        )
                    }
                }
            }
        }
    }
}
