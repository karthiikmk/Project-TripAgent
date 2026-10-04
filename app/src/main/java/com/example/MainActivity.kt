package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.data.model.AppThemeMode
import com.example.data.model.SafeRoutePlan
import com.example.ui.screens.EmergencyHubScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.RouteDetailScreen
import com.example.ui.screens.RoutePlannerScreen
import com.example.ui.screens.SavedRoutesScreen
import com.example.ui.screens.VehicleChecklistScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.RouteViewModel

enum class AppNavScreen {
    HOME,
    PLANNER,
    DETAIL,
    EMERGENCY,
    CHECKLIST,
    SAVED
}

class MainActivity : ComponentActivity() {

    private val viewModel: RouteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            MyApplicationTheme(themeMode = themeMode) {
                MainAppNavHost(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppNavHost(viewModel: RouteViewModel) {
    var currentScreen by remember { mutableStateOf(AppNavScreen.HOME) }
    var previousScreen by remember { mutableStateOf(AppNavScreen.HOME) }
    var selectedRoutePlan by remember { mutableStateOf<SafeRoutePlan?>(null) }
    val carDetails by viewModel.carDetails.collectAsState()

    fun navigateTo(screen: AppNavScreen) {
        previousScreen = currentScreen
        currentScreen = screen
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Only show bottom navigation on primary screens
            if (currentScreen != AppNavScreen.DETAIL) {
                NavigationBar(
                    modifier = Modifier.testTag("main_bottom_nav"),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    NavigationBarItem(
                        selected = currentScreen == AppNavScreen.HOME,
                        onClick = { currentScreen = AppNavScreen.HOME },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Corridors"
                            )
                        },
                        label = { Text("Corridors") },
                        modifier = Modifier.testTag("nav_item_home")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppNavScreen.PLANNER,
                        onClick = { currentScreen = AppNavScreen.PLANNER },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = "Plan Route"
                            )
                        },
                        label = { Text("Plan Trip") },
                        modifier = Modifier.testTag("nav_item_planner")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppNavScreen.EMERGENCY,
                        onClick = { currentScreen = AppNavScreen.EMERGENCY },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Emergency"
                            )
                        },
                        label = { Text("SOS / 1033") },
                        modifier = Modifier.testTag("nav_item_emergency")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppNavScreen.CHECKLIST,
                        onClick = { currentScreen = AppNavScreen.CHECKLIST },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = "My Car"
                            )
                        },
                        label = {
                            val shortName = carDetails.makeModel.split(" ").takeLast(2).joinToString(" ")
                            Text(if (shortName.length <= 11) shortName else "My Car")
                        },
                        modifier = Modifier.testTag("nav_item_checklist")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppNavScreen.SAVED,
                        onClick = { currentScreen = AppNavScreen.SAVED },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = "Saved"
                            )
                        },
                        label = { Text("Saved") },
                        modifier = Modifier.testTag("nav_item_saved")
                    )
                }
            }
        }
    ) { innerPadding ->
        when (currentScreen) {
            AppNavScreen.HOME -> {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToPlanner = { navigateTo(AppNavScreen.PLANNER) },
                    onNavigateToDetail = { plan ->
                        selectedRoutePlan = plan
                        viewModel.selectRoute(plan)
                        navigateTo(AppNavScreen.DETAIL)
                    },
                    onNavigateToEmergency = { navigateTo(AppNavScreen.EMERGENCY) },
                    onNavigateToChecklist = { navigateTo(AppNavScreen.CHECKLIST) },
                    onNavigateToSaved = { navigateTo(AppNavScreen.SAVED) }
                )
            }

            AppNavScreen.PLANNER -> {
                BackHandler { currentScreen = AppNavScreen.HOME }
                RoutePlannerScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentScreen = AppNavScreen.HOME },
                    onNavigateToDetail = { plan ->
                        selectedRoutePlan = plan
                        viewModel.selectRoute(plan)
                        navigateTo(AppNavScreen.DETAIL)
                    }
                )
            }

            AppNavScreen.DETAIL -> {
                BackHandler { currentScreen = previousScreen }
                selectedRoutePlan?.let { plan ->
                    RouteDetailScreen(
                        plan = plan,
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = previousScreen }
                    )
                } ?: run {
                    currentScreen = AppNavScreen.HOME
                }
            }

            AppNavScreen.EMERGENCY -> {
                BackHandler { currentScreen = AppNavScreen.HOME }
                EmergencyHubScreen(
                    onNavigateBack = { currentScreen = AppNavScreen.HOME }
                )
            }

            AppNavScreen.CHECKLIST -> {
                BackHandler { currentScreen = AppNavScreen.HOME }
                VehicleChecklistScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentScreen = AppNavScreen.HOME }
                )
            }

            AppNavScreen.SAVED -> {
                BackHandler { currentScreen = AppNavScreen.HOME }
                SavedRoutesScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentScreen = AppNavScreen.HOME },
                    onNavigateToDetail = { plan ->
                        selectedRoutePlan = plan
                        viewModel.selectRoute(plan)
                        navigateTo(AppNavScreen.DETAIL)
                    }
                )
            }
        }
    }
}
