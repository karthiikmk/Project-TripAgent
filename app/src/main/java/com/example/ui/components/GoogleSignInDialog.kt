package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CarDetails
import com.example.data.model.GoogleUserProfile
import com.example.ui.theme.HighwayGreen
import com.example.ui.theme.NLineBluePrimary
import com.example.ui.theme.NLineRedAccent

@Composable
fun GoogleSignInDialog(
    currentUser: GoogleUserProfile,
    currentCar: CarDetails,
    onDismiss: () -> Unit,
    onSignInSuccess: (email: String, name: String, carDetails: CarDetails) -> Unit
) {
    var emailInput by remember { mutableStateOf(currentUser.email.ifBlank { "mkkarthiik@gmail.com" }) }
    var nameInput by remember { mutableStateOf(currentUser.displayName.ifBlank { "Karthiik" }) }

    // Car Details States
    var makeModelInput by remember { mutableStateOf(currentCar.makeModel) }
    var variantInput by remember { mutableStateOf(currentCar.variant) }
    var regNumInput by remember { mutableStateOf(currentCar.registrationNumber) }
    var tyreSpecInput by remember { mutableStateOf(currentCar.tyreSpecification) }
    var recommendedPsiInput by remember { mutableStateOf(currentCar.recommendedPsi) }
    var fastagInput by remember { mutableStateOf(currentCar.fastagProvider) }
    var bluelinkActive by remember { mutableStateOf(currentCar.hasConnectedCarBluelink) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 24.dp)
                .testTag("google_signin_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header with Google Branding
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "G",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Sign in with Google",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Sync safe routes & configure car profile",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                // Section 1: Google Account Credentials
                Text(
                    text = "Google Account Profile",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("Google Account Email") },
                    placeholder = { Text("your.email@gmail.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("signin_email_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Driver / Account Name") },
                    placeholder = { Text("e.g. Karthiik") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("signin_name_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                // Quick Account Chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable {
                            emailInput = "mkkarthiik@gmail.com"
                            nameInput = "Karthiik"
                        }
                    ) {
                        Text(
                            text = "mkkarthiik@gmail.com",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                // Section 2: Car Details Setup (Updates 4th Tab)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = NLineRedAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Vehicle Details (Updates 4th Tab)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = NLineRedAccent
                        )
                        Text(
                            text = "Customizes highway checklist, tyre pressure & telematics",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                OutlinedTextField(
                    value = makeModelInput,
                    onValueChange = { makeModelInput = it },
                    label = { Text("Car Make & Model") },
                    placeholder = { Text("e.g. Hyundai i20 N Line") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("car_make_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = variantInput,
                        onValueChange = { variantInput = it },
                        label = { Text("Variant") },
                        placeholder = { Text("N8 Turbo DCT") },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f).testTag("car_variant_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = regNumInput,
                        onValueChange = { regNumInput = it },
                        label = { Text("Registration No.") },
                        placeholder = { Text("TN 07 CM 4521") },
                        singleLine = true,
                        modifier = Modifier.weight(1.1f).testTag("car_reg_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = tyreSpecInput,
                        onValueChange = { tyreSpecInput = it },
                        label = { Text("Tyre Specification") },
                        placeholder = { Text("195/55 R16 Alloys") },
                        singleLine = true,
                        modifier = Modifier.weight(1.3f).testTag("car_tyre_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = recommendedPsiInput,
                        onValueChange = { recommendedPsiInput = it },
                        label = { Text("Cold PSI") },
                        placeholder = { Text("33-35 PSI") },
                        singleLine = true,
                        modifier = Modifier.weight(0.9f).testTag("car_psi_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                OutlinedTextField(
                    value = fastagInput,
                    onValueChange = { fastagInput = it },
                    label = { Text("FASTag Account Provider") },
                    placeholder = { Text("Active FASTag (ICICI / Paytm)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("car_fastag_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                // Bluelink / Telematics Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Connected Car / Bluelink Active",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Enables mirror RSA & SOS button assistance",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = bluelinkActive,
                        onCheckedChange = { bluelinkActive = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = HighwayGreen
                        ),
                        modifier = Modifier.testTag("bluelink_toggle_switch")
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Actions
                Button(
                    onClick = {
                        val updatedCar = CarDetails(
                            makeModel = makeModelInput.ifBlank { "Hyundai i20 N Line" },
                            variant = variantInput.ifBlank { "N8 Turbo" },
                            registrationNumber = regNumInput.ifBlank { "TN 07 CM 4521" },
                            tyreSpecification = tyreSpecInput.ifBlank { "195/55 R16 Alloys" },
                            recommendedPsi = recommendedPsiInput.ifBlank { "33-35 PSI Cold" },
                            hasConnectedCarBluelink = bluelinkActive,
                            fastagProvider = fastagInput.ifBlank { "Active FASTag" },
                            fuelType = currentCar.fuelType
                        )
                        onSignInSuccess(emailInput, nameInput, updatedCar)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("confirm_signin_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Sign In",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save Vehicle & Sign In",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
