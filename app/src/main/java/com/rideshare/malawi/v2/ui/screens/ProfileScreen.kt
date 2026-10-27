package com.rideshare.malawi.v2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import com.rideshare.malawi.v2.data.UserRole
import com.rideshare.malawi.v2.data.VerificationStatus
import com.rideshare.malawi.v2.ui.components.PrimaryButton
import com.rideshare.malawi.v2.ui.components.SecondaryButton
import com.rideshare.malawi.v2.ui.components.StatusPill
import com.rideshare.malawi.v2.ui.components.TabHeader
import com.rideshare.malawi.v2.ui.components.toPill
import com.rideshare.malawi.v2.ui.util.displayPhone
import com.rideshare.malawi.v2.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(
    authVm: AuthViewModel,
    onLogout: () -> Unit,
    onOpenVerification: () -> Unit
) {
    val user by authVm.currentUser.collectAsState()

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            TabHeader(title = "Profile")

            Spacer(modifier = Modifier.height(16.dp))

            if (user == null) {
                Text(
                    text = "No user logged in",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val currUser = user!!
                val initial = currUser.name.firstOrNull()?.uppercaseChar()?.toString() ?: "U"

                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Text(
                                text = initial,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = currUser.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = displayPhone(currUser.phone),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = currUser.role.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        if (currUser.role == UserRole.DRIVER) {
                            Spacer(modifier = Modifier.height(16.dp))

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            Spacer(modifier = Modifier.height(16.dp))

                            val pill = currUser.verification.toPill()
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Verification status",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                StatusPill(label = pill.label, tone = pill.tone)
                            }

                            if (currUser.vehicleModel.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Vehicle",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${currUser.vehicleModel} (${currUser.vehicleReg})",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            if (currUser.verification != VerificationStatus.VERIFIED) {
                                Spacer(modifier = Modifier.height(16.dp))
                                PrimaryButton(
                                    text = "Open verification",
                                    onClick = onOpenVerification
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                SecondaryButton(
                    text = "Log out",
                    onClick = {
                        authVm.logout()
                        onLogout()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
