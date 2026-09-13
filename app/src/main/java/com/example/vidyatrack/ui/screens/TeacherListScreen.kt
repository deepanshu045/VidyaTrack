package com.example.vidyatrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vidyatrack.data.remote.TeacherResponse
import com.example.vidyatrack.ui.viewmodel.TeacherListUiState
import com.example.vidyatrack.ui.viewmodel.TeacherListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherListScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: TeacherListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Teachers") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                Icon(Icons.Default.Add, contentDescription = "Add Teacher")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (uiState) {
                is TeacherListUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is TeacherListUiState.Success -> {
                    val teachers = (uiState as TeacherListUiState.Success).teachers
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(teachers) { teacher ->
                            TeacherItem(teacher = teacher)
                        }
                    }
                }
                is TeacherListUiState.Error -> {
                    Text(
                        text = (uiState as TeacherListUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun TeacherItem(teacher: TeacherResponse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = teacher.fullName, style = MaterialTheme.typography.titleMedium)
            Text(text = teacher.email, style = MaterialTheme.typography.bodyMedium)
            teacher.contactNumber?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
