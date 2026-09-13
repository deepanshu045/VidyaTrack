package com.example.vidyatrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vidyatrack.ui.viewmodel.AddEditStudentUiState
import com.example.vidyatrack.ui.viewmodel.AddEditStudentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditStudentScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditStudentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add/Edit Student") },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = viewModel.fullName.value,
                    onValueChange = { viewModel.fullName.value = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = viewModel.dob.value,
                    onValueChange = { viewModel.dob.value = it },
                    label = { Text("Date of Birth (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) }
                )

                DropdownField(
                    label = "Gender",
                    options = listOf("Male", "Female", "Other"),
                    selectedOption = viewModel.gender.value,
                    onOptionSelected = { viewModel.gender.value = it }
                )

                OutlinedTextField(
                    value = viewModel.parentName.value,
                    onValueChange = { viewModel.parentName.value = it },
                    label = { Text("Parent Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = viewModel.parentContact.value,
                    onValueChange = { viewModel.parentContact.value = it },
                    label = { Text("Parent Contact") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = viewModel.address.value,
                    onValueChange = { viewModel.address.value = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = viewModel.admissionDate.value,
                    onValueChange = { viewModel.admissionDate.value = it },
                    label = { Text("Admission Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) }
                )

                DropdownField(
                    label = "Status",
                    options = listOf("Active", "Inactive"),
                    selectedOption = viewModel.status.value,
                    onOptionSelected = { viewModel.status.value = it }
                )

                Button(
                    onClick = { viewModel.saveStudent(onNavigateBack) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is AddEditStudentUiState.Loading
                ) {
                    if (uiState is AddEditStudentUiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Save Student")
                    }
                }

                if (uiState is AddEditStudentUiState.Error) {
                    Text(
                        text = (uiState as AddEditStudentUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
