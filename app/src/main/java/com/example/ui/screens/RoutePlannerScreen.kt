package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Toll
import androidx.compose.material.icons.filled.TripOrigin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import com.example.ui.components.HighwayChipsRow
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.PlaceAutocompleteService
import com.example.data.api.PlaceSuggestion
import com.example.data.model.SafeRoutePlan
import com.example.ui.theme.HighwayGreen
import com.example.ui.theme.NLineBluePrimary
import com.example.ui.theme.NLineRedAccent
import com.example.ui.viewmodel.PlanUiState
import com.example.ui.viewmodel.RouteViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RoutePlannerScreen(
    viewModel: RouteViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (SafeRoutePlan) -> Unit
) {
    var originInput by remember { mutableStateOf("") }
    var destinationInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }

    // Autocomplete states
    var originSuggestions by remember { mutableStateOf<List<PlaceSuggestion>>(emptyList()) }
    var destinationSuggestions by remember { mutableStateOf<List<PlaceSuggestion>>(emptyList()) }
    var showOriginDropdown by remember { mutableStateOf(false) }
    var showDestinationDropdown by remember { mutableStateOf(false) }

    val planState by viewModel.planUiState.collectAsState()
    val googlePlacesKey by viewModel.googlePlacesApiKey.collectAsState()

    // Query free OpenStreetMap / Google Places API for origin with debouncing
    LaunchedEffect(originInput, showOriginDropdown) {
        if (showOriginDropdown) {
            delay(120)
            originSuggestions = PlaceAutocompleteService.searchPlaces(originInput, googlePlacesKey)
        } else {
            originSuggestions = emptyList()
        }
    }

    // Query free OpenStreetMap / Google Places API for destination with debouncing
    LaunchedEffect(destinationInput, showDestinationDropdown) {
        if (showDestinationDropdown) {
            delay(120)
            destinationSuggestions = PlaceAutocompleteService.searchPlaces(destinationInput, googlePlacesKey)
        } else {
            destinationSuggestions = emptyList()
        }
    }

    // Auto-navigate immediately to RouteDetailScreen only ONCE per safe route generation
    LaunchedEffect(planState) {
        val state = planState
        if (state is PlanUiState.Success && viewModel.lastNavigatedPlanId != state.plan.id) {
            viewModel.lastNavigatedPlanId = state.plan.id
            onNavigateToDetail(state.plan)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Safe Route Planner",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("route_planner_screen"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Objective Callout
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Safety Priority",
                                tint = HighwayGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CORE SAFETY MANDATE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = HighwayGreen,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "The goal is the safest good road, not the shortest. Distance and duration do not matter. A longer divided highway is better than a shorter village road, ghat shortcut, or city cut.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Input Fields Card with Autocomplete Dropdowns
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Route Origin & Destination",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        // 1. Origin Field with Dropdown Autocomplete
                        Column {
                            OutlinedTextField(
                                value = originInput,
                                onValueChange = {
                                    originInput = it
                                    showOriginDropdown = true
                                },
                                label = { Text("Start (Place + City)") },
                                placeholder = { Text("Type place, e.g. Adyar, Electronic City, Bandra...") },
                                leadingIcon = {
                                    Surface(
                                        shape = CircleShape,
                                        color = NLineBluePrimary,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "S",
                                                color = Color.White,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                },
                                trailingIcon = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (originInput.isNotBlank()) {
                                            IconButton(onClick = {
                                                originInput = ""
                                                showOriginDropdown = false
                                            }) {
                                                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                                            }
                                        }
                                        IconButton(onClick = { showOriginDropdown = !showOriginDropdown }) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = "Suggestions",
                                                tint = if (showOriginDropdown) NLineBluePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("origin_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )

                            // Origin Autocomplete Dropdown
                            AnimatedVisibility(visible = showOriginDropdown && originSuggestions.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shadowElevation = 3.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(6.dp)) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "MAP DATABASE SUGGESTIONS",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "Tap to select",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 9.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                        originSuggestions.forEach { suggestion ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        originInput = suggestion.fullFormatted
                                                        showOriginDropdown = false
                                                    }
                                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.LocationOn,
                                                    contentDescription = null,
                                                    tint = NLineBluePrimary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = suggestion.title,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = suggestion.subtitle,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 2. Destination Field with Dropdown Autocomplete
                        Column {
                            OutlinedTextField(
                                value = destinationInput,
                                onValueChange = {
                                    destinationInput = it
                                    showDestinationDropdown = true
                                },
                                label = { Text("Destination (Place + City / Landmark)") },
                                placeholder = { Text("Type place, e.g. Bengaluru, Goa, Tirupati...") },
                                leadingIcon = {
                                    Surface(
                                        shape = CircleShape,
                                        color = NLineRedAccent,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "D",
                                                color = Color.White,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                },
                                trailingIcon = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (destinationInput.isNotBlank()) {
                                            IconButton(onClick = {
                                                destinationInput = ""
                                                showDestinationDropdown = false
                                            }) {
                                                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                                            }
                                        }
                                        IconButton(onClick = { showDestinationDropdown = !showDestinationDropdown }) {
                                            Icon(
                                                imageVector = Icons.Default.Place,
                                                contentDescription = "Suggestions",
                                                tint = if (showDestinationDropdown) NLineRedAccent else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("destination_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )

                            // Destination Autocomplete Dropdown
                            AnimatedVisibility(visible = showDestinationDropdown && destinationSuggestions.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shadowElevation = 3.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(6.dp)) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "MAP DATABASE SUGGESTIONS",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = NLineRedAccent
                                            )
                                            Text(
                                                text = "Tap to select",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 9.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                        destinationSuggestions.forEach { suggestion ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        destinationInput = suggestion.fullFormatted
                                                        showDestinationDropdown = false
                                                    }
                                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Place,
                                                    contentDescription = null,
                                                    tint = NLineRedAccent,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = suggestion.title,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = suggestion.subtitle,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = notesInput,
                            onValueChange = { notesInput = it },
                            label = { Text("Specific trip requirements (Optional)") },
                            placeholder = { Text("e.g. Night travel, ghat caution, monsoon check") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("notes_input"),
                            shape = RoundedCornerShape(10.dp),
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Quick Corridors Chips
                        Text(
                            text = "Quick Presets:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val suggestions = listOf(
                                "Chennai" to "Bengaluru",
                                "Mumbai" to "Goa",
                                "Delhi" to "Jaipur",
                                "Bengaluru" to "Mysuru",
                                "Chennai" to "Tirupati"
                            )

                            suggestions.forEach { (orig, dest) ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable {
                                        originInput = "$orig, India"
                                        destinationInput = "$dest, India"
                                        showOriginDropdown = false
                                        showDestinationDropdown = false
                                    }
                                ) {
                                    Text(
                                        text = "$orig → $dest",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Plan Button
                        Button(
                            onClick = {
                                showOriginDropdown = false
                                showDestinationDropdown = false
                                viewModel.lastNavigatedPlanId = null
                                viewModel.planCustomRoute(
                                    origin = originInput,
                                    destination = destinationInput,
                                    notes = notesInput
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("execute_plan_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            enabled = planState !is PlanUiState.Loading && originInput.isNotBlank() && destinationInput.isNotBlank()
                        ) {
                            if (planState is PlanUiState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generating Safe Route...")
                            } else {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Generate Safe Route",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Results Section
            when (val state = planState) {
                is PlanUiState.Loading -> {
                    // Handled beautifully by the full screen overlay dialog at the bottom
                }

                is PlanUiState.Error -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Routing Error",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }

                is PlanUiState.Success -> {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onNavigateToDetail(state.plan) }
                                .testTag("generated_plan_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = HighwayGreen
                                    ) {
                                        Text(
                                            text = "AUDITED SAFE ROUTE READY",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "View Details",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = state.plan.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Route S, D, and Stops with clear icons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = NLineBluePrimary,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "S",
                                                color = Color.White,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = state.plan.origin,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "to",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = CircleShape,
                                        color = NLineRedAccent,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "D",
                                                color = Color.White,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = state.plan.destination,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                if (state.plan.stops.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = CircleShape,
                                            color = HighwayGreen,
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "T",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${state.plan.stops.size} Pass-through Toll Pins • ${state.plan.distanceApprox}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = HighwayGreen,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                HighwayChipsRow(verifyLine = state.plan.verifyLine)

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        onNavigateToDetail(state.plan)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("view_generated_plan_detail_button"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Navigation,
                                        contentDescription = "View Plan",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Open Full Route Plan & Maps Link", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                is PlanUiState.Idle -> {
                    // Nothing to show
                }
            }
        }

        // Prominent Centered Loading Spinner & Auditing Card
        if (planState is PlanUiState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.55f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.5.dp,
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Auditing Highways, Toll Plazas & Flyovers...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Enforcing divided carriageways, median barriers, and pass-through toll locks to prevent broken village shortcuts.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
}

