package com.fracorbas.motivationapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.tooling.preview.Preview
import com.fracorbas.motivationapp.data.model.Habit
import com.fracorbas.motivationapp.data.model.TimeOfDay
import com.fracorbas.motivationapp.ui.components.AppTopBar
import com.fracorbas.motivationapp.ui.components.EmptyState
import com.fracorbas.motivationapp.ui.components.HabitRow
import com.fracorbas.motivationapp.ui.components.HabitsLazyList
import com.fracorbas.motivationapp.ui.components.SearchField
import com.fracorbas.motivationapp.ui.components.SectionLabel
import com.fracorbas.motivationapp.ui.components.StatTile
import com.fracorbas.motivationapp.ui.theme.MotivationAppTheme
import com.fracorbas.motivationapp.viewmodel.HabitViewModel
import kotlinx.coroutines.launch

/** View modes for displaying habits */
enum class HabitViewMode { LIST, TIMELINE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onAddHabitClick: () -> Unit,
    onHabitClick: (Int) -> Unit,
    onEditHabitClick: (Int) -> Unit,
    onStatisticsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HabitViewModel = hiltViewModel()
) {
    val habits by viewModel.filteredHabits.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val todayCompletedCount by viewModel.todayCompletedCount.collectAsState()
    val activeHabitsCount by viewModel.activeHabitsCount.collectAsState()

    var viewMode by remember { mutableStateOf(HabitViewMode.LIST) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is HabitViewModel.UiEvent.ShowSnackbar ->
                    scope.launch { snackbarHostState.showSnackbar(event.message) }
                is HabitViewModel.UiEvent.NavigateToAddHabit ->
                    if (event.habitId != null) onHabitClick(event.habitId) else onAddHabitClick()
                HabitViewModel.UiEvent.NavigateBack -> {}
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(title = "Habitudes", actions = {
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Default.Settings, contentDescription = "Réglages")
                }
                IconButton(onClick = onStatisticsClick) {
                    Icon(Icons.Default.Assessment, contentDescription = "Statistiques")
                }
                SingleChoiceSegmentedButtonRow {
                    SegmentedButton(
                        selected = viewMode == HabitViewMode.LIST,
                        onClick = { viewMode = HabitViewMode.LIST },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = "Liste", modifier = Modifier.size(18.dp))
                    }
                    SegmentedButton(
                        selected = viewMode == HabitViewMode.TIMELINE,
                        onClick = { viewMode = HabitViewMode.TIMELINE },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = "Calendrier", modifier = Modifier.size(18.dp))
                    }
                }
            })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddHabitClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter une habitude")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Today's progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatTile(
                    value = todayCompletedCount,
                    label = "Aujourd'hui",
                    accent = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    value = activeHabitsCount,
                    label = "Actives",
                    accent = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SearchField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                placeholder = "Rechercher une habitude"
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionLabel("Vos habitudes", Modifier.weight(1f))
                Text(
                    text = "${habits.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (habits.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Schedule,
                    title = if (searchQuery.isBlank()) "Aucune habitude" else "Aucun résultat",
                    hint = if (searchQuery.isBlank()) "Appuyez sur + pour en créer une"
                    else "Essayez une autre recherche",
                    modifier = Modifier.padding(top = 32.dp)
                )
            } else {
                when (viewMode) {
                    HabitViewMode.LIST -> HabitsLazyList(
                        habits = habits,
                        onToggleCompletion = { viewModel.toggleHabitCompletion(it) },
                        onEditClick = onEditHabitClick,
                        onDeleteClick = { viewModel.deleteHabit(it) },
                        onToggleNotification = { id, enabled -> viewModel.toggleNotification(id, enabled) },
                        onHabitClick = onHabitClick
                    )
                    HabitViewMode.TIMELINE -> TimelineHabitsList(
                        habits = habits,
                        onToggleCompletion = { viewModel.toggleHabitCompletion(it) },
                        onEditClick = onEditHabitClick,
                        onDeleteClick = { viewModel.deleteHabit(it) },
                        onToggleNotification = { id, enabled -> viewModel.toggleNotification(id, enabled) },
                        onHabitClick = onHabitClick
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    MotivationAppTheme {
        MainScreen(
            onAddHabitClick = {},
            onHabitClick = {},
            onEditHabitClick = {},
            onStatisticsClick = {},
            onSettingsClick = {},
            viewModel = hiltViewModel()
        )
    }
}
