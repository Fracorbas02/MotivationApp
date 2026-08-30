package com.fracorbas.motivationapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.tooling.preview.Preview
import com.fracorbas.motivationapp.data.model.NotificationFrequency
import com.fracorbas.motivationapp.data.model.NotificationFrequencyUnit
import com.fracorbas.motivationapp.data.model.Trigger
import com.fracorbas.motivationapp.ui.components.AppTopBar
import com.fracorbas.motivationapp.ui.components.SectionLabel
import com.fracorbas.motivationapp.ui.theme.MotivationAppTheme
import com.fracorbas.motivationapp.viewmodel.HabitViewModel
import com.fracorbas.motivationapp.viewmodel.TriggerViewModel
import kotlinx.coroutines.launch
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreen(
    habitId: Int? = null,
    onBack: () -> Unit,
    onManageTriggers: () -> Unit,
    habitViewModel: HabitViewModel = hiltViewModel(),
    triggerViewModel: TriggerViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedTrigger by remember { mutableStateOf<Trigger?>(null) }
    var triggerText by remember { mutableStateOf("") }
    var reminderTime by remember { mutableStateOf<LocalTime?>(null) }
    var notificationEnabled by remember { mutableStateOf(false) }
    var isActive by remember { mutableStateOf(true) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showTriggerDropdown by remember { mutableStateOf(false) }
    var notificationFrequency by remember { mutableStateOf<Int?>(null) }
    var notificationFrequencyUnit by remember { mutableStateOf<NotificationFrequencyUnit?>(null) }
    var showFrequencyDropdown by remember { mutableStateOf(false) }

    val allTriggers by triggerViewModel.allTriggers.collectAsState()

    LaunchedEffect(habitId) {
        if (habitId != null) {
            val habit = habitViewModel.getHabitById(habitId) ?: return@LaunchedEffect
            title = habit.title
            description = habit.description ?: ""
            triggerText = habit.trigger
            reminderTime = habit.reminderTime
            notificationEnabled = habit.notificationEnabled
            isActive = habit.isActive
            habit.triggerId?.let { selectedTrigger = triggerViewModel.getTriggerById(it) }
            notificationFrequency = habit.notificationFrequency
            notificationFrequencyUnit = habit.notificationFrequencyUnit?.let { NotificationFrequencyUnit.fromString(it) }
        }
    }

    LaunchedEffect(Unit) {
        habitViewModel.uiEvent.collect { event ->
            when (event) {
                is HabitViewModel.UiEvent.ShowSnackbar ->
                    scope.launch { snackbarHostState.showSnackbar(event.message); onBack() }
                HabitViewModel.UiEvent.NavigateBack -> onBack()
                is HabitViewModel.UiEvent.NavigateToAddHabit -> {}
            }
        }
    }

    val fieldShape = RoundedCornerShape(14.dp)
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { AppTopBar(if (habitId == null) "Nouvelle habitude" else "Modifier l'habitude", onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            SectionLabel("Identité")
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Titre") },
                placeholder = { Text("Ex : Lire 10 pages") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = fieldShape,
                colors = fieldColors,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optionnel)") },
                placeholder = { Text("Ex : Un livre de développement personnel") },
                modifier = Modifier.fillMaxWidth(),
                shape = fieldShape,
                colors = fieldColors,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            SectionLabel("Déclencheur")
            ExposedDropdownMenuBox(
                expanded = showTriggerDropdown,
                onExpandedChange = { showTriggerDropdown = !showTriggerDropdown },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedTrigger?.name ?: triggerText,
                    onValueChange = { triggerText = it; selectedTrigger = null },
                    label = { Text("Sélectionner ou créer un déclencheur") },
                    placeholder = { Text("Ex : Après mon café du matin") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = "Ouvrir") },
                    singleLine = true,
                    shape = fieldShape,
                    colors = fieldColors,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                if (allTriggers.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = showTriggerDropdown,
                        onDismissRequest = { showTriggerDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Créer un nouveau déclencheur")
                                }
                            },
                            onClick = { showTriggerDropdown = false; onManageTriggers() }
                        )
                        allTriggers.forEach { trigger ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                        Text(trigger.name)
                                        Spacer(Modifier.weight(1f))
                                        if (trigger.isCustom) Text("Perso", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                },
                                onClick = { selectedTrigger = trigger; triggerText = trigger.name; showTriggerDropdown = false }
                            )
                        }
                    }
                }
            }
            if (selectedTrigger?.description?.isNotBlank() == true) {
                Text(
                    text = selectedTrigger!!.description!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            SectionLabel("Rappel")
            OutlinedTextField(
                value = reminderTime?.let { "%02d:%02d".format(it.hour, it.minute) } ?: "",
                onValueChange = {},
                label = { Text("Heure de rappel") },
                placeholder = { Text("Sélectionner une heure") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTimePicker = true },
                readOnly = true,
                shape = fieldShape,
                colors = fieldColors,
                trailingIcon = {
                    IconButton(onClick = { showTimePicker = true }) {
                        Icon(Icons.Default.Schedule, contentDescription = "Choisir une heure")
                    }
                }
            )
            if (reminderTime == null) {
                Text(
                    text = "Aucune heure définie",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            SettingRow(
                label = "Notification",
                checked = notificationEnabled,
                onCheckedChange = { notificationEnabled = it }
            )

            if (notificationEnabled) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionLabel("Fréquence de notification")
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = notificationFrequency?.toString() ?: "",
                            onValueChange = { notificationFrequency = it.toIntOrNull() },
                            label = { Text("Tous les") },
                            placeholder = { Text("1") },
                            modifier = Modifier.width(120.dp),
                            singleLine = true,
                            shape = fieldShape,
                            colors = fieldColors,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                        )
                        ExposedDropdownMenuBox(
                            expanded = showFrequencyDropdown,
                            onExpandedChange = { showFrequencyDropdown = !showFrequencyDropdown },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = notificationFrequencyUnit?.pluralDisplayName ?: "Jours",
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = "Ouvrir") },
                                shape = fieldShape,
                                colors = fieldColors
                            )
                            ExposedDropdownMenu(
                                expanded = showFrequencyDropdown,
                                onDismissRequest = { showFrequencyDropdown = false }
                            ) {
                                NotificationFrequencyUnit.getAll().forEach { unit ->
                                    DropdownMenuItem(
                                        text = { Text(unit.pluralDisplayName) },
                                        onClick = { notificationFrequencyUnit = unit; showFrequencyDropdown = false }
                                    )
                                }
                            }
                        }
                    }
                    if (notificationFrequency != null && notificationFrequencyUnit != null) {
                        Text(
                            text = NotificationFrequency(notificationFrequency!!, notificationFrequencyUnit!!).getDisplayText(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            SettingRow(
                label = "Habitude active",
                checked = isActive,
                onCheckedChange = { isActive = it }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val finalTriggerText = selectedTrigger?.name ?: triggerText
                    if (habitId == null) {
                        habitViewModel.createHabit(
                            title = title,
                            description = if (description.isBlank()) null else description,
                            trigger = finalTriggerText,
                            triggerId = selectedTrigger?.id,
                            reminderTime = reminderTime,
                            notificationEnabled = notificationEnabled,
                            notificationFrequency = notificationFrequency,
                            notificationFrequencyUnit = notificationFrequencyUnit?.name
                        )
                    } else {
                        habitViewModel.updateHabit(
                            id = habitId,
                            title = title,
                            description = if (description.isBlank()) null else description,
                            trigger = finalTriggerText,
                            triggerId = selectedTrigger?.id,
                            reminderTime = reminderTime,
                            notificationEnabled = notificationEnabled,
                            isActive = isActive,
                            notificationFrequency = notificationFrequency,
                            notificationFrequencyUnit = notificationFrequencyUnit?.name
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank() && (selectedTrigger != null || triggerText.isNotBlank()),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Sauvegarder")
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = reminderTime?.hour ?: 8,
            initialMinute = reminderTime?.minute ?: 0,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    reminderTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Annuler") }
            },
            title = { Text("Choisir une heure") },
            text = {
                TimePicker(
                    state = timePickerState,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
    }
}

@Composable
private fun SettingRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Preview(showBackground = true)
@Composable
private fun AddHabitScreenPreview() {
    MotivationAppTheme {
        AddHabitScreen(onBack = {}, onManageTriggers = {})
    }
}
