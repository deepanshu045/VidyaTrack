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
import com.example.vidyatrack.data.remote.ClassResponse
import com.example.vidyatrack.ui.viewmodel.ClassListUiState
import com.example.vidyatrack.ui.viewmodel.ClassListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassListScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ClassListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Classes") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                Icon(Icons.Default.Add, contentDescription = "Add Class")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (uiState) {
                is ClassListUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ClassListUiState.Success -> {
                    val classes = (uiState as ClassListUiState.Success).classes
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(classes) { classItem ->
                            ClassItem(
                                classItem = classItem,
                                onClick = { onNavigateToDetail(classItem.id) }
                            )
                        }
                    }
                }
                is ClassListUiState.Error -> {
                    Text(
                        text = (uiState as ClassListUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun ClassItem(classItem: ClassResponse, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = classItem.name, style = MaterialTheme.typography.titleMedium)
            classItem.description?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
