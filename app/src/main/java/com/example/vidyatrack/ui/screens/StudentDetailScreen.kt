package com.example.vidyatrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vidyatrack.ui.viewmodel.StudentDetailUiState
import com.example.vidyatrack.ui.viewmodel.StudentDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Int) -> Unit,
    viewModel: StudentDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState is StudentDetailUiState.Success) {
                        val student = (uiState as StudentDetailUiState.Success).student
                        IconButton(onClick = { onNavigateToEdit(student.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { viewModel.deleteStudent(onNavigateBack) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
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
                is StudentDetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is StudentDetailUiState.Success -> {
                    val student = (uiState as StudentDetailUiState.Success).student
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        DetailItem("Full Name", student.fullName)
                        DetailItem("Date of Birth", student.dob)
                        DetailItem("Gender", student.gender)
                        DetailItem("Parent Name", student.parentName ?: "N/A")
                        DetailItem("Parent Contact", student.parentContact ?: "N/A")
                        DetailItem("Address", student.address ?: "N/A")
                        DetailItem("Admission Date", student.admissionDate ?: "N/A")
                        DetailItem("Status", student.status)
                    }
                }
                is StudentDetailUiState.Error -> {
                    Text(
                        text = (uiState as StudentDetailUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}
