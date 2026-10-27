package com.rideshare.malawi.v2.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import com.rideshare.malawi.v2.data.VerificationStatus
import com.rideshare.malawi.v2.ui.components.ErrorText
import com.rideshare.malawi.v2.ui.components.PrimaryButton
import com.rideshare.malawi.v2.ui.components.ScreenScaffold
import com.rideshare.malawi.v2.ui.components.SecondaryButton
import com.rideshare.malawi.v2.ui.components.StatusPill
import com.rideshare.malawi.v2.ui.components.toPill
import com.rideshare.malawi.v2.viewmodel.AuthViewModel

@Composable
fun DriverVerificationScreen(
    authVm: AuthViewModel,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    val currentUser by authVm.currentUser.collectAsState()

    var vehicleModel by remember { mutableStateOf(currentUser?.vehicleModel ?: "") }
    var vehicleReg by remember { mutableStateOf(currentUser?.vehicleReg ?: "") }
    var documentUri by remember { mutableStateOf(currentUser?.documentUri) }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        documentUri = uri?.toString()
        authVm.clearError()
    }

    val status = currentUser?.verification ?: VerificationStatus.NOT_SUBMITTED
    val pill = status.toPill()

    ScreenScaffold(
        title = "Driver verification",
        onBack = onBack
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Current status:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            StatusPill(label = pill.label, tone = pill.tone)
        }

        Spacer(modifier = Modifier.height(20.dp))

        authVm.error?.let { err ->
            ErrorText(message = err)
            Spacer(modifier = Modifier.height(12.dp))
        }

        when (status) {
            VerificationStatus.NOT_SUBMITTED -> {
                OutlinedTextField(
                    value = vehicleModel,
                    onValueChange = {
                        vehicleModel = it
                        authVm.clearError()
                    },
                    label = { Text("Vehicle make & model") },
                    placeholder = { Text("e.g. Toyota Hiace") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = vehicleReg,
                    onValueChange = {
                        vehicleReg = it
                        authVm.clearError()
                    },
                    label = { Text("Vehicle registration number") },
                    placeholder = { Text("e.g. BT 4521") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Registration document",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            MaterialTheme.shapes.medium
                        )
                        .clickable { pickerLauncher.launch("image/*") }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.UploadFile,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val text = if (documentUri.isNullOrBlank()) {
                            "Attach registration document (tap to select image)"
                        } else {
                            "Registration document attached (tap to change)"
                        }
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                PrimaryButton(
                    text = "Submit for verification",
                    onClick = {
                        authVm.submitVerification(vehicleModel, vehicleReg, documentUri)
                    }
                )
            }

            VerificationStatus.PENDING -> {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Your verification details have been submitted and are pending administrative review.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                SecondaryButton(
                    text = "Simulate admin approval",
                    onClick = {
                        authVm.simulateApproval()
                    }
                )
            }

            VerificationStatus.VERIFIED -> {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Your driver account is verified! You can now offer rides.",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                PrimaryButton(
                    text = "Go to my rides",
                    onClick = onDone
                )
            }
        }
    }
}
