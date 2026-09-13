package com.example.vidyatrack.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vidyatrack.data.remote.StudentResponse
import com.example.vidyatrack.data.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentListViewModel @Inject constructor(
    private val repository: StudentRepository
) : ViewModel() {

    private val _uiState = mutableStateOf<StudentListUiState>(StudentListUiState.Loading)
    val uiState: State<StudentListUiState> = _uiState

    private var allStudents = listOf<StudentResponse>()

    init {
        loadStudents()
    }

    fun loadStudents() {
        _uiState.value = StudentListUiState.Loading
        viewModelScope.launch {
            repository.getStudents()
                .onSuccess { students ->
                    allStudents = students
                    _uiState.value = StudentListUiState.Success(students)
                }
                .onFailure { error ->
                    _uiState.value = StudentListUiState.Error(error.message ?: "Unknown error")
                }
        }
    }

    fun searchStudents(query: String) {
        val filtered = if (query.isEmpty()) {
            allStudents
        } else {
            allStudents.filter { it.fullName.contains(query, ignoreCase = true) }
        }
        _uiState.value = StudentListUiState.Success(filtered)
    }

    fun deleteStudent(id: Int) {
        viewModelScope.launch {
            repository.deleteStudent(id)
                .onSuccess {
                    loadStudents()
                }
        }
    }
}

sealed class StudentListUiState {
    object Loading : StudentListUiState()
    data class Success(val students: List<StudentResponse>) : StudentListUiState()
    data class Error(val message: String) : StudentListUiState()
}
