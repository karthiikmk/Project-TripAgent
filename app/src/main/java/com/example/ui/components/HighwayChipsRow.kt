package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HighwayChipsRow(verifyLine: String, modifier: Modifier = Modifier) {
    val marker = "Road numbers used:"
    if (verifyLine.contains(marker, ignoreCase = true)) {
        val parts = verifyLine.split(marker, ignoreCase = true)
        val prefix = parts.firstOrNull()?.trim() ?: "✅"
        val cleanPrefix = if (prefix.startsWith("- ")) prefix.substringAfter("- ").trim() else prefix
        val highwayPart = parts.getOrNull(1)?.trim() ?: ""
        
        // Strip trailing period or non-alphanumeric trailing chars from the highway parts
        val cleanHighways = highwayPart.removeSuffix(".").trim()
        
        // Split by comma or space to get the individual shields
        val highwayTokens = cleanHighways.split(",").map { it.trim() }.filter { it.isNotBlank() }
        
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            FlowRow(
                verticalArrangement = Arrangement.Center,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                highwayTokens.forEach { token ->
                    Surface(
                        color = Color(0xFFF59E0B), // Vibrant yellow/orange like Google Maps highway shields
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.testTag("highway_chip_$token")
                    ) {
                        Text(
                            text = token,
                            color = Color.Black,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    } else {
        Text(
            text = verifyLine,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier
        )
    }
}
