package com.example.vidyatrack.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vidyatrack.data.remote.ClassResponse
import com.example.vidyatrack.data.repository.ClassRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClassListViewModel @Inject constructor(private val repository: ClassRepository) : ViewModel() {
    private val _uiState = mutableStateOf<ClassListUiState>(ClassListUiState.Loading)
    val uiState: State<ClassListUiState> = _uiState
    private val _message = mutableStateOf<String?>(null)
    val message: State<String?> = _message

    init { loadClasses() }

    fun loadClasses() {
        _uiState.value = ClassListUiState.Loading
        viewModelScope.launch {
            repository.getClasses().onSuccess { _uiState.value = ClassListUiState.Success(it) }
                .onFailure { _uiState.value = ClassListUiState.Error(it.message ?: "Failed to load classes") }
        }
    }

    fun updateClass(id: Int, name: String, description: String) {
        if (name.isBlank()) { _message.value = "Class name is required"; return }
        viewModelScope.launch {
            repository.updateClass(id, mapOf("name" to name.trim(), "description" to description.trim()))
                .onSuccess { _message.value = "Class updated successfully"; loadClasses() }
                .onFailure { _message.value = it.message ?: "Failed to update class" }
        }
    }

    fun deleteClass(id: Int) {
        viewModelScope.launch {
            repository.deleteClass(id)
                .onSuccess { _message.value = "Class deleted successfully"; loadClasses() }
                .onFailure { _message.value = it.message ?: "Failed to delete class" }
        }
    }

    fun clearMessage() { _message.value = null }
}

sealed class ClassListUiState {
    object Loading : ClassListUiState()
    data class Success(val classes: List<ClassResponse>) : ClassListUiState()
    data class Error(val message: String) : ClassListUiState()
}
