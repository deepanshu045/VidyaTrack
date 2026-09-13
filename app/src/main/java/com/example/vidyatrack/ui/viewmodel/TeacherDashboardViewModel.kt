package com.example.vidyatrack.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vidyatrack.data.remote.TeacherSummaryResponse
import com.example.vidyatrack.data.repository.AuthRepository
import com.example.vidyatrack.data.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeacherDashboardViewModel @Inject constructor(
    private val repository: DashboardRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = mutableStateOf<TeacherDashboardUiState>(TeacherDashboardUiState.Loading)
    val uiState: State<TeacherDashboardUiState> = _uiState

    init {
        loadSummary()
    }

    fun loadSummary() {
        _uiState.value = TeacherDashboardUiState.Loading
        viewModelScope.launch {
            repository.getTeacherSummary()
                .onSuccess { summary ->
                    _uiState.value = TeacherDashboardUiState.Success(summary)
                }
                .onFailure { error ->
                    _uiState.value = TeacherDashboardUiState.Error(error.message ?: "Unknown error")
                }
        }
    }

    fun logout() {
        authRepository.logout()
    }
}

sealed class TeacherDashboardUiState {
    object Loading : TeacherDashboardUiState()
    data class Success(val summary: TeacherSummaryResponse) : TeacherDashboardUiState()
    data class Error(val message: String) : TeacherDashboardUiState()
}
