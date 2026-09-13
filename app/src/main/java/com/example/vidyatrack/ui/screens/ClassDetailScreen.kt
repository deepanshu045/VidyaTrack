package com.example.vidyatrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vidyatrack.ui.viewmodel.ClassDetailUiState
import com.example.vidyatrack.ui.viewmodel.ClassDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassDetailScreen(
    onNavigateBack: () -> Unit,
    onViewReports: (Int) -> Unit = {},
    viewModel: ClassDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState
    val availableStudents by viewModel.availableStudents
    val availableTeachers by viewModel.availableTeachers
    var showStudentDialog by remember { mutableStateOf(false) }
    var showTeacherDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var actionMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text((uiState as? ClassDetailUiState.Success)?.classInfo?.name ?: "Class Detail") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (uiState) {
                is ClassDetailUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is ClassDetailUiState.Error -> Text((uiState as ClassDetailUiState.Error).message, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                is ClassDetailUiState.Success -> {
                    val state = uiState as ClassDetailUiState.Success
                    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
                        item {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = { onViewReports(state.classInfo.id) }) { Text("Reports") }
                                TextButton(onClick = { showEditDialog = true }) { Text("Edit") }
                                TextButton(onClick = { showDeleteDialog = true }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                            }
                            Text("Info", style = MaterialTheme.typography.titleLarge)
                            Text(state.classInfo.description ?: "No description", style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(24.dp))
                        }
                        item {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Teachers", style = MaterialTheme.typography.titleLarge)
                                TextButton(onClick = { showTeacherDialog = true }) { Icon(Icons.Default.Add, null); Text("Assign") }
                            }
                        }
                        items(state.teachers) { teacher -> Text(teacher.fullName, Modifier.padding(vertical = 4.dp)) }
                        item { Spacer(Modifier.height(24.dp)) }
                        item {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Students", style = MaterialTheme.typography.titleLarge)
                                TextButton(onClick = { showStudentDialog = true }) { Icon(Icons.Default.Add, null); Text("Assign") }
                            }
                        }
                        items(state.students) { student -> Text(student.fullName, Modifier.padding(vertical = 4.dp)) }
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        val current = uiState as? ClassDetailUiState.Success
        var name by remember(current) { mutableStateOf(current?.classInfo?.name ?: "") }
        var description by remember(current) { mutableStateOf(current?.classInfo?.description ?: "") }
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Class") },
            text = {
                Column {
                    OutlinedTextField(name, { name = it }, label = { Text("Class name") }, singleLine = true)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(description, { description = it }, label = { Text("Description") }, minLines = 3)
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.updateClass(name, description) { ok -> actionMessage = if (ok) "Class updated successfully" else "Failed to update class" }; showEditDialog = false }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showEditDialog = false }) { Text("Cancel") } }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Class?") },
            text = { Text("This will permanently delete the class. Continue?") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteClass { ok -> if (ok) onNavigateBack() else actionMessage = "Failed to delete class" }; showDeleteDialog = false }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }

    if (showStudentDialog) {
        val ids = (uiState as? ClassDetailUiState.Success)?.students?.map { it.id } ?: emptyList()
        val selectable = availableStudents.filter { it.id !in ids }
        AlertDialog(onDismissRequest = { showStudentDialog = false }, title = { Text("Assign Student") }, text = { LazyColumn(Modifier.heightIn(max = 400.dp)) { items(selectable) { student -> TextButton({ viewModel.assignStudent(student.id); showStudentDialog = false }, Modifier.fillMaxWidth()) { Text(student.fullName) } } } }, confirmButton = { TextButton({ showStudentDialog = false }) { Text("Close") } })
    }

    if (showTeacherDialog) {
        val ids = (uiState as? ClassDetailUiState.Success)?.teachers?.map { it.id } ?: emptyList()
        val selectable = availableTeachers.filter { it.id !in ids }
        AlertDialog(onDismissRequest = { showTeacherDialog = false }, title = { Text("Assign Teacher") }, text = { LazyColumn(Modifier.heightIn(max = 400.dp)) { items(selectable) { teacher -> TextButton({ viewModel.assignTeacher(teacher.id); showTeacherDialog = false }, Modifier.fillMaxWidth()) { Text(teacher.fullName) } } } }, confirmButton = { TextButton({ showTeacherDialog = false }) { Text("Close") } })
    }

    actionMessage?.let { message ->
        AlertDialog(onDismissRequest = { actionMessage = null }, title = { Text("Class") }, text = { Text(message) }, confirmButton = { TextButton({ actionMessage = null }) { Text("OK") } })
    }
}
