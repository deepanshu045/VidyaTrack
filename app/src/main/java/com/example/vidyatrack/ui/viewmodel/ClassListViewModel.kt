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
class ClassListViewModel @Inject constructor(
    private val repository: ClassRepository
) : ViewModel() {

    private val _uiState = mutableStateOf<ClassListUiState>(ClassListUiState.Loading)
    val uiState: State<ClassListUiState> = _uiState

    init {
        loadClasses()
    }

    fun loadClasses() {
        _uiState.value = ClassListUiState.Loading
        viewModelScope.launch {
            repository.getClasses()
                .onSuccess { classes ->
                    _uiState.value = ClassListUiState.Success(classes)
                }
                .onFailure { error ->
                    _uiState.value = ClassListUiState.Error(error.message ?: "Unknown error")
                }
        }
    }
}

sealed class ClassListUiState {
    object Loading : ClassListUiState()
    data class Success(val classes: List<ClassResponse>) : ClassListUiState()
    data class Error(val message: String) : ClassListUiState()
}
