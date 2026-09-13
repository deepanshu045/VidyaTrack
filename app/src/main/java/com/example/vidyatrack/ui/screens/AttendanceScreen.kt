package com.example.vidyatrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vidyatrack.ui.viewmodel.AttendanceUiState
import com.example.vidyatrack.ui.viewmodel.AttendanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    onNavigateBack: () -> Unit,
    viewModel: AttendanceViewModel = hiltViewModel()
) {
    val state by viewModel.uiState
    val statuses by viewModel.statuses
    val history by viewModel.history
    val saving by viewModel.saving
    val message by viewModel.message

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mark Attendance") },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) { Text("Back") }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (state) {
                AttendanceUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is AttendanceUiState.Error -> Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
                is AttendanceUiState.Success -> {
                    val success = state
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text("Date: ${success.date}", style = MaterialTheme.typography.titleMedium)
                            Text("Tap Present or Absent for each student.")
                        }

                        items(success.students, key = { it.id }) { student ->
                            Card(Modifier.fillMaxWidth()) {
                                Row(
                                    Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(student.fullName, modifier = Modifier.weight(1f))
                                    Row {
                                        FilterChip(
                                            selected = statuses[student.id] == "Present",
                                            onClick = { viewModel.setStatus(student.id, "Present") },
                                            label = { Text("Present") }
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        FilterChip(
                                            selected = statuses[student.id] == "Absent",
                                            onClick = { viewModel.setStatus(student.id, "Absent") },
                                            label = { Text("Absent") }
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Button(
                                onClick = { viewModel.save() },
                                enabled = !saving && success.students.isNotEmpty(),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (saving) CircularProgressIndicator(Modifier.size(18.dp))
                                else Text("Save Attendance")
                            }
                        }

                        item {
                            Text("Attendance History", style = MaterialTheme.typography.titleLarge)
                        }

                        if (history.isEmpty()) {
                            item { Text("No attendance history yet.") }
                        } else {
                            items(history, key = { it.id }) { item ->
                                ListItem(
                                    headlineContent = { Text(item.date) },
                                    supportingContent = {
                                        val percentage = if (item.total_count == 0) 0.0
                                        else item.present_count * 100.0 / item.total_count
                                        Text("${item.present_count}/${item.total_count} present • ${"%.1f".format(percentage)}%")
                                    }
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }

    if (message != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearMessage() },
            title = { Text("Attendance") },
            text = { Text(message ?: "") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearMessage() }) { Text("OK") }
            }
        )
    }
}
