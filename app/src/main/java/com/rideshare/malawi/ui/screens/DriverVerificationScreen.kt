package com.rideshare.malawi.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rideshare.malawi.data.AppState
import com.rideshare.malawi.data.VerificationStatus
import com.rideshare.malawi.ui.components.*

@Composable
fun DriverVerificationScreen(
    onBack: () -> Unit,
    onContinueToHome: () -> Unit,
    onHome: () -> Unit
) {
    var vehicleModel by remember { mutableStateOf("") }
    var vehicleReg by remember { mutableStateOf("") }
    var documentAttached by remember { mutableStateOf(false) }

    val status = AppState.driverVerification.value
    val canSubmit = vehicleModel.isNotBlank() && vehicleReg.isNotBlank() && documentAttached

    ScreenScaffold(title = "Driver verification", onBack = onBack, onHome = onHome) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Status: ", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            val (label, tone) = status.toPill()
            StatusPill(label, tone)
        }

        Spacer(Modifier.height(24.dp))

        if (status == VerificationStatus.VERIFIED) {
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("You're verified and ready to offer rides.", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(16.dp))
                    PrimaryButton(text = "Go to driver home", onClick = onContinueToHome)
                }
            }
            return@ScreenScaffold
        }

        OutlinedTextField(
            value = vehicleModel,
            onValueChange = { vehicleModel = it },
            label = { Text("Vehicle make & model") },
            placeholder = { Text("e.g. Toyota Hiace") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = vehicleReg,
            onValueChange = { vehicleReg = it },
            label = { Text("Vehicle registration number") },
            placeholder = { Text("e.g. BT 4521") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(20.dp))

        Text("Registration document", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))

        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
                .clickable { documentAttached = !documentAttached }
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(16.dp)) {
                Text(
                    if (documentAttached) "Document attached (tap to remove)" else "Attach a photo of your registration document",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        PrimaryButton(
            text = "Submit for verification",
            enabled = canSubmit
        ) {
            AppState.driverVerification.value = VerificationStatus.VERIFIED
        }
    }
}
