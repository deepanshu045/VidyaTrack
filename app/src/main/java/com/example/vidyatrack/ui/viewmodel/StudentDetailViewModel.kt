package com.example.vidyatrack.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vidyatrack.data.remote.StudentResponse
import com.example.vidyatrack.data.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentDetailViewModel @Inject constructor(
    private val repository: StudentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val studentId: Int = checkNotNull(savedStateHandle.get<String>("studentId")).toInt()

    private val _uiState = mutableStateOf<StudentDetailUiState>(StudentDetailUiState.Loading)
    val uiState: State<StudentDetailUiState> = _uiState

    init {
        loadStudent()
    }

    fun loadStudent() {
        _uiState.value = StudentDetailUiState.Loading
        viewModelScope.launch {
            repository.getStudent(studentId)
                .onSuccess { student ->
                    _uiState.value = StudentDetailUiState.Success(student)
                }
                .onFailure { error ->
                    _uiState.value = StudentDetailUiState.Error(error.message ?: "Unknown error")
                }
        }
    }

    fun deleteStudent(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteStudent(studentId)
                .onSuccess {
                    onDeleted()
                }
        }
    }
}

sealed class StudentDetailUiState {
    object Loading : StudentDetailUiState()
    data class Success(val student: StudentResponse) : StudentDetailUiState()
    data class Error(val message: String) : StudentDetailUiState()
}
