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
import com.fracorbas.motivationapp.ui.HabitListScreen
import com.fracorbas.motivationapp.ui.MainScreen
import com.fracorbas.motivationapp.ui.theme.MotivationAppTheme
import com.fracorbas.motivationapp.viewmodel.HabitViewModel
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
    val viewModel: HabitViewModel = hiltViewModel()

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
                viewModel = viewModel
            )
        }
        composable("addHabit") {
            AddHabitScreen(
                onBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        }
        composable("addHabit/{habitId}") { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId")?.toIntOrNull()
            AddHabitScreen(
                habitId = habitId,
                onBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        }
    }
}
