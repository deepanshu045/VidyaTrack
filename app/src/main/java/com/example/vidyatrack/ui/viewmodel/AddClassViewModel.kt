package com.example.vidyatrack.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vidyatrack.data.repository.ClassRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddClassViewModel @Inject constructor(
    private val repository: ClassRepository
) : ViewModel() {

    private val _uiState = mutableStateOf<AddClassUiState>(AddClassUiState.Idle)
    val uiState: State<AddClassUiState> = _uiState

    fun addClass(name: String, description: String) {
        _uiState.value = AddClassUiState.Loading
        viewModelScope.launch {
            val classMap = mapOf(
                "name" to name,
                "description" to description
            )
            repository.addClass(classMap)
                .onSuccess {
                    _uiState.value = AddClassUiState.Success
                }
                .onFailure { error ->
                    _uiState.value = AddClassUiState.Error(error.message ?: "Unknown error")
                }
        }
    }
}

sealed class AddClassUiState {
    object Idle : AddClassUiState()
    object Loading : AddClassUiState()
    object Success : AddClassUiState()
    data class Error(val message: String) : AddClassUiState()
}
