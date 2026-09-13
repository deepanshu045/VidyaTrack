package com.example.vidyatrack.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vidyatrack.data.remote.TeacherResponse
import com.example.vidyatrack.data.repository.TeacherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeacherListViewModel @Inject constructor(
    private val repository: TeacherRepository
) : ViewModel() {

    private val _uiState = mutableStateOf<TeacherListUiState>(TeacherListUiState.Loading)
    val uiState: State<TeacherListUiState> = _uiState

    init {
        loadTeachers()
    }

    fun loadTeachers() {
        _uiState.value = TeacherListUiState.Loading
        viewModelScope.launch {
            repository.getTeachers()
                .onSuccess { teachers ->
                    _uiState.value = TeacherListUiState.Success(teachers)
                }
                .onFailure { error ->
                    _uiState.value = TeacherListUiState.Error(error.message ?: "Unknown error")
                }
        }
    }
}

sealed class TeacherListUiState {
    object Loading : TeacherListUiState()
    data class Success(val teachers: List<TeacherResponse>) : TeacherListUiState()
    data class Error(val message: String) : TeacherListUiState()
}
