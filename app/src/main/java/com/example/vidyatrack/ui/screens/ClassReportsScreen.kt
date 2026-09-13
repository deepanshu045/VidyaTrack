package com.example.vidyatrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vidyatrack.ui.viewmodel.ClassReportsUiState
import com.example.vidyatrack.ui.viewmodel.ClassReportsViewModel

@Composable
fun ClassReportsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ClassReportsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Attendance Reports") },
            navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "Back") } }
        )
    }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                ClassReportsUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is ClassReportsUiState.Error -> Column(Modifier.align(Alignment.Center).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = viewModel::load) { Text("Retry") }
                }
                is ClassReportsUiState.Success -> LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { Text("Student Performance", style = MaterialTheme.typography.titleLarge) }
                    items(state.performance) { student ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text(student.student_name, style = MaterialTheme.typography.titleMedium)
                                Text("Present: ${student.present_count}  •  Absent: ${student.absent_count}")
                                Text("Sessions: ${student.total_sessions}  •  Attendance: ${"%.1f".format(student.attendance_percentage)}%")
                            }
                        }
                    }
                    item {
                        Spacer(Modifier.height(12.dp))
                        Text("Low Attendance (below 75%)", style = MaterialTheme.typography.titleLarge)
                    }
                    if (state.lowAttendance.isEmpty()) item { Text("No students are below 75% attendance.") }
                    items(state.lowAttendance) { student ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text(student.student_name, style = MaterialTheme.typography.titleMedium)
                                Text("Attendance: ${"%.1f".format(student.attendance_percentage)}%")
                            }
                        }
                    }
                }
            }
        }
    }
}
