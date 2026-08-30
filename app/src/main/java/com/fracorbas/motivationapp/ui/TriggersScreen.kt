package com.fracorbas.motivationapp.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.tooling.preview.Preview
import com.fracorbas.motivationapp.data.model.Trigger
import com.fracorbas.motivationapp.ui.components.AppTopBar
import com.fracorbas.motivationapp.ui.components.EmptyState
import com.fracorbas.motivationapp.ui.components.SearchField
import com.fracorbas.motivationapp.ui.components.SectionLabel
import com.fracorbas.motivationapp.ui.theme.MotivationAppTheme
import com.fracorbas.motivationapp.viewmodel.TriggerViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TriggersScreen(
    onBack: () -> Unit,
    onAddTriggerClick: () -> Unit,
    onEditTriggerClick: (Int) -> Unit,
    viewModel: TriggerViewModel = hiltViewModel()
) {
    val triggers by viewModel.allTriggers.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }

    val filtered = remember(triggers, searchQuery) {
        if (searchQuery.isBlank()) triggers
        else triggers.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is TriggerViewModel.UiEvent.ShowSnackbar ->
                    scope.launch { snackbarHostState.showSnackbar(event.message) }
                is TriggerViewModel.UiEvent.NavigateToAddTrigger ->
                    if (event.triggerId != null) onEditTriggerClick(event.triggerId) else onAddTriggerClick()
                TriggerViewModel.UiEvent.NavigateBack -> onBack()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { AppTopBar("Déclencheurs", onBack) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onAddTriggerClick() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            SearchField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Rechercher un déclencheur"
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionLabel("Tous les déclencheurs", Modifier.weight(1f))
                Text(
                    text = "${filtered.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(8.dp))

            if (filtered.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Add,
                    title = if (searchQuery.isBlank()) "Aucun déclencheur" else "Aucun résultat",
                    hint = if (searchQuery.isBlank()) "Appuyez sur + pour en créer un"
                    else "Essayez une autre recherche",
                    modifier = Modifier.padding(top = 24.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered) { trigger ->
                        TriggerCard(
                            trigger = trigger,
                            onEditClick = { viewModel.onEditTriggerClick(trigger.id) },
                            onDeleteClick = { viewModel.deleteTrigger(trigger) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TriggerCard(
    trigger: Trigger,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(14.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = trigger.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (trigger.description != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = trigger.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (trigger.isCustom) "Personnalisé" else "Par défaut",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (trigger.isCustom) {
                IconButton(onClick = onEditClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Modifier",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.85f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTriggerScreen(
    triggerId: Int? = null,
    onBack: () -> Unit,
    viewModel: TriggerViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    LaunchedEffect(triggerId) {
        if (triggerId != null) {
            val trigger = viewModel.getTriggerById(triggerId)
            if (trigger != null) {
                name = trigger.name
                description = trigger.description ?: ""
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is TriggerViewModel.UiEvent.ShowSnackbar ->
                    scope.launch { snackbarHostState.showSnackbar(event.message) }
                TriggerViewModel.UiEvent.NavigateBack -> onBack()
                is TriggerViewModel.UiEvent.NavigateToAddTrigger -> {}
            }
        }
    }

    val fieldShape = RoundedCornerShape(14.dp)
    val fieldColors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { AppTopBar(if (triggerId == null) "Nouveau déclencheur" else "Modifier le déclencheur", onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            SectionLabel("Détails")
            androidx.compose.material3.OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nom du déclencheur") },
                placeholder = { Text("Ex : Après le café du matin") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = fieldShape,
                colors = fieldColors
            )
            androidx.compose.material3.OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optionnel)") },
                placeholder = { Text("Ex : Après mon premier café de la journée") },
                modifier = Modifier.fillMaxWidth(),
                shape = fieldShape,
                colors = fieldColors
            )
            Spacer(Modifier.height(8.dp))
            androidx.compose.material3.Button(
                onClick = {
                    if (triggerId == null) {
                        viewModel.createTrigger(name, if (description.isBlank()) null else description)
                    } else {
                        viewModel.updateTrigger(triggerId, name, if (description.isBlank()) null else description)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Sauvegarder")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TriggersScreenPreview() {
    MotivationAppTheme {
        TriggersScreen(onBack = {}, onAddTriggerClick = {}, onEditTriggerClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun AddTriggerScreenPreview() {
    MotivationAppTheme {
        AddTriggerScreen(onBack = {})
    }
}
