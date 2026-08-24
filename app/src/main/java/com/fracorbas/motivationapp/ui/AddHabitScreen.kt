package com.fracorbas.motivationapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fracorbas.motivationapp.data.model.Habit
import com.fracorbas.motivationapp.ui.theme.MotivationAppTheme
import com.fracorbas.motivationapp.viewmodel.HabitViewModel
import kotlinx.coroutines.launch
import java.time.LocalTime

/**
 * Screen for adding or editing a habit.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreen(
    habitId: Int? = null,
    onBack: () -> Unit,
    viewModel: HabitViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // State for the form
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var trigger by remember { mutableStateOf("") }
    var reminderTime by remember { mutableStateOf<LocalTime?>(null) }
    var notificationEnabled by remember { mutableStateOf(false) }
    var isActive by remember { mutableStateOf(true) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    // Load habit data if editing
    LaunchedEffect(habitId) {
        if (habitId != null) {
            val habit = viewModel.getHabitById(habitId)
            if (habit != null) {
                title = habit.title
                description = habit.description ?: ""
                trigger = habit.trigger
                reminderTime = habit.reminderTime
                notificationEnabled = habit.notificationEnabled
                isActive = habit.isActive
            }
        }
    }

    // Handle UI events
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is HabitViewModel.UiEvent.ShowSnackbar -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(event.message)
                        onBack()
                    }
                }
                HabitViewModel.UiEvent.NavigateBack -> {
                    onBack()
                }
                is HabitViewModel.UiEvent.NavigateToAddHabit -> {}
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (habitId == null) "Ajouter une habitude" 
                        else "Modifier l'habitude",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Titre") },
                placeholder = { Text("Ex: Lire 10 pages") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                )
            )

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optionnel)") },
                placeholder = { Text("Ex: Lire un livre de développement personnel") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                )
            )

            // Trigger
            OutlinedTextField(
                value = trigger,
                onValueChange = { trigger = it },
                label = { Text("Élément déclencheur") },
                placeholder = { Text("Ex: Après mon café du matin") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                )
            )

            // Reminder Time
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = reminderTime?.let { "${it.hour}:${it.minute.toString().padStart(2, '0')}" } ?: "",
                    onValueChange = {},
                    label = { Text("Heure de rappel") },
                    placeholder = { Text("Sélectionner une heure") },
                    modifier = Modifier.weight(1f),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showTimePicker = true }) {
                            Icon(Icons.Default.Schedule, contentDescription = "Choisir une heure")
                        }
                    }
                )
                
                Spacer(modifier = Modifier.size(8.dp))
                
                // Notification toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickableWithRipple {
                        notificationEnabled = !notificationEnabled
                    }
                ) {
                    Checkbox(
                        checked = notificationEnabled,
                        onCheckedChange = { notificationEnabled = it }
                    )
                    Text("Notification")
                }
            }

            // Active toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickableWithRipple {
                        isActive = !isActive
                    }
            ) {
                Checkbox(
                    checked = isActive,
                    onCheckedChange = { isActive = it }
                )
                Text("Habitude active")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = {
                    if (habitId == null) {
                        viewModel.createHabit(
                            title = title,
                            description = if (description.isBlank()) null else description,
                            trigger = trigger,
                            reminderTime = reminderTime,
                            notificationEnabled = notificationEnabled
                        )
                    } else {
                        viewModel.updateHabit(
                            id = habitId,
                            title = title,
                            description = if (description.isBlank()) null else description,
                            trigger = trigger,
                            reminderTime = reminderTime,
                            notificationEnabled = notificationEnabled,
                            isActive = isActive
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank() && trigger.isNotBlank(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Sauvegarder",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("Sauvegarder")
            }
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = reminderTime?.hour ?: 8,
            initialMinute = reminderTime?.minute ?: 0,
            is24Hour = true
        )
        
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            onConfirm = {
                reminderTime = LocalTime.of(
                    timePickerState.hour,
                    timePickerState.minute
                )
                showTimePicker = false
            },
            state = timePickerState
        )
    }
}

// Extension for clickable with ripple effect
fun Modifier.clickableWithRipple(onClick: () -> Unit): Modifier {
    return this.then(
        androidx.compose.foundation.clickable(
            onClick = onClick,
            indication = androidx.compose.material.ripple.rememberRipple()
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    state: TimePickerState
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Choisir une heure",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TimePicker(state = state)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onDismissRequest,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text("Annuler")
                }
                
                Spacer(modifier = Modifier.size(8.dp))
                
                Button(onClick = onConfirm) {
                    Text("OK")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddHabitScreenPreview() {
    MotivationAppTheme {
        AddHabitScreen(
            onBack = {},
            viewModel = hiltViewModel()
        )
    }
}
