package com.example.vidyatrack.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vidyatrack.data.remote.StudentPerformanceResponse
import com.example.vidyatrack.data.repository.AttendanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClassReportsViewModel @Inject constructor(
    private val repository: AttendanceRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val classId: Int = checkNotNull(savedStateHandle["classId"])

    private val _uiState = mutableStateOf<ClassReportsUiState>(ClassReportsUiState.Loading)
    val uiState: State<ClassReportsUiState> = _uiState

    init { load() }

    fun load() {
        _uiState.value = ClassReportsUiState.Loading
        viewModelScope.launch {
            val performance = repository.getPerformance(classId)
            val lowAttendance = repository.getLowAttendance(classId, 75.0)
            if (performance.isSuccess && lowAttendance.isSuccess) {
                _uiState.value = ClassReportsUiState.Success(
                    performance.getOrThrow(), lowAttendance.getOrThrow()
                )
            } else {
                _uiState.value = ClassReportsUiState.Error(
                    performance.exceptionOrNull()?.message
                        ?: lowAttendance.exceptionOrNull()?.message
                        ?: "Failed to load attendance reports"
                )
            }
        }
    }
}

sealed class ClassReportsUiState {
    object Loading : ClassReportsUiState()
    data class Success(
        val performance: List<StudentPerformanceResponse>,
        val lowAttendance: List<StudentPerformanceResponse>
    ) : ClassReportsUiState()
    data class Error(val message: String) : ClassReportsUiState()
}
