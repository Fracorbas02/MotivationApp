package com.fracorbas.motivationapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fracorbas.motivationapp.ui.AddHabitScreen
import com.fracorbas.motivationapp.ui.AddTriggerScreen
import com.fracorbas.motivationapp.ui.HabitDetailScreen
import com.fracorbas.motivationapp.ui.HomeScreen
import com.fracorbas.motivationapp.ui.MainScreen
import com.fracorbas.motivationapp.ui.SettingsScreen
import com.fracorbas.motivationapp.ui.StatisticsScreen
import com.fracorbas.motivationapp.ui.TriggersScreen
import com.fracorbas.motivationapp.ui.components.AppBottomBar
import com.fracorbas.motivationapp.ui.components.TopTab
import com.fracorbas.motivationapp.ui.theme.MotivationAppTheme
import com.fracorbas.motivationapp.viewmodel.BackupViewModel
import com.fracorbas.motivationapp.viewmodel.HabitViewModel
import com.fracorbas.motivationapp.viewmodel.SettingsViewModel
import com.fracorbas.motivationapp.viewmodel.StatisticsViewModel
import com.fracorbas.motivationapp.viewmodel.TriggerViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity for the MotivationApp.
 *
 * This activity hosts the Jetpack Compose navigation graph.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val settings by settingsViewModel.settings.collectAsState()

            MotivationAppTheme(
                themeMode = settings.themeMode,
                dynamicColor = settings.dynamicColor
            ) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MotivationAppNavigation()
                }
            }
        }
    }
}

/**
 * Navigation graph for the MotivationApp, with a bottom bar for the three
 * top-level destinations (Accueil, Habitudes, Stats). Sub-screens (detail,
 * edit, settings, triggers) hide the bar and show a back arrow instead.
 */
@Composable
fun MotivationAppNavigation() {
    val navController = rememberNavController()
    val habitViewModel: HabitViewModel = hiltViewModel()
    val triggerViewModel: TriggerViewModel = hiltViewModel()
    val statisticsViewModel: StatisticsViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val backupViewModel: BackupViewModel = hiltViewModel()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in TopTab.routes

    fun navigateToTab(tab: TopTab) {
        navController.navigate(tab.route) {
            // Pop back to the start destination to avoid a growing stack.
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AppBottomBar(currentRoute = currentRoute, onTabSelected = ::navigateToTab)
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = TopTab.HOME.route
        ) {
            composable(TopTab.HOME.route) {
                HomeScreen(
                    onSettingsClick = { navController.navigate("settings") },
                    viewModel = habitViewModel,
                    contentPadding = padding
                )
            }
            composable(TopTab.HABITS.route) {
                MainScreen(
                    onAddHabitClick = { navController.navigate("addHabit") },
                    onHabitClick = { habitId ->
                        navController.navigate("habitDetail/$habitId")
                    },
                    onEditHabitClick = { habitId ->
                        navController.navigate("addHabit/$habitId")
                    },
                    viewModel = habitViewModel,
                    contentPadding = padding
                )
            }
            composable(TopTab.STATS.route) {
                StatisticsScreen(
                    viewModel = statisticsViewModel,
                    contentPadding = padding
                )
            }

            composable("habitDetail/{habitId}") { backStackEntry ->
                val habitId = backStackEntry.arguments?.getString("habitId")?.toIntOrNull() ?: 0
                HabitDetailScreen(
                    habitId = habitId,
                    onBack = { navController.popBackStack() },
                    onEditHabit = { id -> navController.navigate("addHabit/$id") },
                    viewModel = habitViewModel
                )
            }
            composable("addHabit") {
                AddHabitScreen(
                    onBack = { navController.popBackStack() },
                    onManageTriggers = { navController.navigate("triggers") },
                    habitViewModel = habitViewModel,
                    triggerViewModel = triggerViewModel
                )
            }
            composable("addHabit/{habitId}") { backStackEntry ->
                val habitId = backStackEntry.arguments?.getString("habitId")?.toIntOrNull()
                AddHabitScreen(
                    habitId = habitId,
                    onBack = { navController.popBackStack() },
                    onManageTriggers = { navController.navigate("triggers") },
                    habitViewModel = habitViewModel,
                    triggerViewModel = triggerViewModel
                )
            }

            // Triggers management screens
            composable("triggers") {
                TriggersScreen(
                    onBack = { navController.popBackStack() },
                    onAddTriggerClick = { navController.navigate("addTrigger") },
                    onEditTriggerClick = { triggerId ->
                        navController.navigate("addTrigger/$triggerId")
                    },
                    viewModel = triggerViewModel
                )
            }

            composable("addTrigger") {
                AddTriggerScreen(
                    onBack = { navController.popBackStack() },
                    viewModel = triggerViewModel
                )
            }

            composable("addTrigger/{triggerId}") { backStackEntry ->
                val triggerId = backStackEntry.arguments?.getString("triggerId")?.toIntOrNull()
                AddTriggerScreen(
                    triggerId = triggerId,
                    onBack = { navController.popBackStack() },
                    viewModel = triggerViewModel
                )
            }

            // Settings screen
            composable("settings") {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    viewModel = settingsViewModel,
                    backupViewModel = backupViewModel
                )
            }
        }
    }
}
