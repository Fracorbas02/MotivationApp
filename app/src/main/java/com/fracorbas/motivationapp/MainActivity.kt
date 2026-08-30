package com.fracorbas.motivationapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fracorbas.motivationapp.ui.AddHabitScreen
import com.fracorbas.motivationapp.ui.AddTriggerScreen
import com.fracorbas.motivationapp.ui.MainScreen
import com.fracorbas.motivationapp.ui.StatisticsScreen
import com.fracorbas.motivationapp.ui.TriggersScreen
import com.fracorbas.motivationapp.ui.theme.MotivationAppTheme
import com.fracorbas.motivationapp.viewmodel.HabitViewModel
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
            MotivationAppTheme {
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
 * Navigation graph for the MotivationApp.
 */
@androidx.compose.runtime.Composable
fun MotivationAppNavigation() {
    val navController = rememberNavController()
    val habitViewModel: HabitViewModel = hiltViewModel()
    val triggerViewModel: TriggerViewModel = hiltViewModel()
    val statisticsViewModel: StatisticsViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            MainScreen(
                onAddHabitClick = { navController.navigate("addHabit") },
                onHabitClick = { habitId ->
                    navController.navigate("addHabit/$habitId")
                },
                onStatisticsClick = { navController.navigate("statistics") },
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
        
        // Statistics screen
        composable("statistics") {
            StatisticsScreen(
                onBack = { navController.popBackStack() },
                viewModel = statisticsViewModel
            )
        }
    }
}
