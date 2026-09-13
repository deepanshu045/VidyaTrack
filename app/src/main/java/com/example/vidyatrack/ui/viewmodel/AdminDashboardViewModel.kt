package com.example.vidyatrack.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vidyatrack.data.remote.AdminStatsResponse
import com.example.vidyatrack.data.repository.AuthRepository
import com.example.vidyatrack.data.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val repository: DashboardRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = mutableStateOf<AdminDashboardUiState>(AdminDashboardUiState.Loading)
    val uiState: State<AdminDashboardUiState> = _uiState

    init {
        loadStats()
    }

    fun loadStats() {
        _uiState.value = AdminDashboardUiState.Loading
        viewModelScope.launch {
            repository.getAdminStats()
                .onSuccess { stats ->
                    _uiState.value = AdminDashboardUiState.Success(stats)
                }
                .onFailure { error ->
                    _uiState.value = AdminDashboardUiState.Error(error.message ?: "Unknown error")
                }
        }
    }

    fun logout() {
        authRepository.logout()
    }
}

sealed class AdminDashboardUiState {
    object Loading : AdminDashboardUiState()
    data class Success(val stats: AdminStatsResponse) : AdminDashboardUiState()
    data class Error(val message: String) : AdminDashboardUiState()
}
