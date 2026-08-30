package com.fracorbas.motivationapp.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/** Top-level tab shown in the bottom navigation bar. */
enum class TopTab(val route: String, val label: String, val icon: ImageVector) {
    HOME("home", "Accueil", Icons.Default.Home),
    HABITS("main", "Habitudes", Icons.AutoMirrored.Filled.FormatListBulleted),
    STATS("statistics", "Stats", Icons.Default.Assessment);

    companion object {
        /** Routes that show the bottom bar. */
        val routes: Set<String> = entries.map { it.route }.toSet()
        fun fromRoute(route: String?): TopTab? = entries.firstOrNull { it.route == route }
    }
}

/**
 * Material 3 bottom navigation bar for the three top-level destinations.
 * Hidden on detail/edit screens.
 */
@Composable
fun AppBottomBar(
    currentRoute: String?,
    onTabSelected: (TopTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        TopTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = { onTabSelected(tab) },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) }
            )
        }
    }
}
