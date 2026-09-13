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
    viewModel: ClassDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState
    val availableStudents by viewModel.availableStudents
    val availableTeachers by viewModel.availableTeachers

    var showStudentDialog by remember { mutableStateOf(false) }
    var showTeacherDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (uiState) {
                            is ClassDetailUiState.Success -> (uiState as ClassDetailUiState.Success).classInfo.name
                            else -> "Class Detail"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (uiState) {
                is ClassDetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ClassDetailUiState.Success -> {
                    val successState = uiState as ClassDetailUiState.Success
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        item {
                            Text(text = "Info", style = MaterialTheme.typography.titleLarge)
                            Text(text = successState.classInfo.description ?: "No description", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Teachers", style = MaterialTheme.typography.titleLarge)
                                TextButton(onClick = { showTeacherDialog = true }) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Text("Assign")
                                }
                            }
                        }

                        items(successState.teachers) { teacher ->
                            Text(text = teacher.fullName, modifier = Modifier.padding(vertical = 4.dp))
                        }

                        item { Spacer(modifier = Modifier.height(24.dp)) }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Students", style = MaterialTheme.typography.titleLarge)
                                TextButton(onClick = { showStudentDialog = true }) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Text("Assign")
                                }
                            }
                        }

                        items(successState.students) { student ->
                            Text(text = student.fullName, modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
                is ClassDetailUiState.Error -> {
                    Text(
                        text = (uiState as ClassDetailUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }

    if (showStudentDialog) {
        val enrolledStudentIds = (uiState as? ClassDetailUiState.Success)?.students?.map { it.id } ?: emptyList()
        val selectableStudents = availableStudents.filter { it.id !in enrolledStudentIds }

        AlertDialog(
            onDismissRequest = { showStudentDialog = false },
            title = { Text("Assign Student") },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(selectableStudents) { student ->
                        TextButton(
                            onClick = {
                                viewModel.assignStudent(student.id)
                                showStudentDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(student.fullName)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStudentDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showTeacherDialog) {
        val assignedTeacherIds = (uiState as? ClassDetailUiState.Success)?.teachers?.map { it.id } ?: emptyList()
        val selectableTeachers = availableTeachers.filter { it.id !in assignedTeacherIds }

        AlertDialog(
            onDismissRequest = { showTeacherDialog = false },
            title = { Text("Assign Teacher") },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(selectableTeachers) { teacher ->
                        TextButton(
                            onClick = {
                                viewModel.assignTeacher(teacher.id)
                                showTeacherDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(teacher.fullName)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTeacherDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
