package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HighwayGreen
import com.example.ui.theme.NLineBluePrimary
import com.example.ui.theme.NLineRedAccent

@Composable
fun EmergencyQuickCallList(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        EmergencyContactRow(
            title = "Hyundai Roadside Assistance",
            subtitle = "Fuel up to 5L, puncture & tow to nearest workshop",
            number = "1800 102 4645",
            rawDialNumber = "18001024645",
            icon = Icons.Default.Build,
            accentColor = NLineBluePrimary,
            tag = "call_hyundai_rsa"
        ) { dialPhone(context, "18001024645") }

        EmergencyContactRow(
            title = "Highway Blocked / Crash (NHAI)",
            subtitle = "Patrol, route clearance, towing & crane across NH corridors",
            number = "1033",
            rawDialNumber = "1033",
            icon = Icons.Default.Navigation,
            accentColor = HighwayGreen,
            tag = "call_nhai_1033"
        ) { dialPhone(context, "1033") }

        EmergencyContactRow(
            title = "All Emergency Response",
            subtitle = "Unified National Emergency Support Center",
            number = "112",
            rawDialNumber = "112",
            icon = Icons.Default.Shield,
            accentColor = NLineRedAccent,
            tag = "call_emergency_112"
        ) { dialPhone(context, "112") }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EmergencyMiniCard(
                title = "Police",
                number = "100",
                icon = Icons.Default.LocalPolice,
                color = Color(0xFF2563EB),
                modifier = Modifier.weight(1f)
            ) { dialPhone(context, "100") }

            EmergencyMiniCard(
                title = "Ambulance",
                number = "108",
                icon = Icons.Default.LocalHospital,
                color = Color(0xFFDC2626),
                modifier = Modifier.weight(1f)
            ) { dialPhone(context, "108") }

            EmergencyMiniCard(
                title = "Fire",
                number = "101",
                icon = Icons.Default.Warning,
                color = Color(0xFFEA580C),
                modifier = Modifier.weight(1f)
            ) { dialPhone(context, "101") }
        }
    }
}

@Composable
private fun EmergencyContactRow(
    title: String,
    subtitle: String,
    number: String,
    rawDialNumber: String,
    icon: ImageVector,
    accentColor: Color,
    tag: String,
    onCallClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCallClick() }
            .testTag(tag),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
                Text(
                    text = number,
                    style = MaterialTheme.typography.labelLarge,
                    color = accentColor,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Button(
                onClick = onCallClick,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call $number",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Call", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EmergencyMiniCard(
    title: String,
    number: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("emergency_mini_$number"),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = number,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

fun dialPhone(context: Context, phoneNumber: String) {
    val clean = phoneNumber.replace("\\s+".toRegex(), "")
    val intent = Intent(Intent.ACTION_DIAL).apply {
        data = Uri.parse("tel:$clean")
    }
    context.startActivity(intent)
}
