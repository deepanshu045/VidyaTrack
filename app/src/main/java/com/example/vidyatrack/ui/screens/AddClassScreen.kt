package com.example.vidyatrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vidyatrack.ui.viewmodel.AddClassUiState
import com.example.vidyatrack.ui.viewmodel.AddClassViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddClassScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddClassViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        if (uiState is AddClassUiState.Success) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Class") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Class Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState is AddClassUiState.Error) {
                Text(
                    text = (uiState as AddClassUiState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = {
                    viewModel.addClass(name, description)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is AddClassUiState.Loading
            ) {
                if (uiState is AddClassUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Add Class")
                }
            }
        }
    }
}
