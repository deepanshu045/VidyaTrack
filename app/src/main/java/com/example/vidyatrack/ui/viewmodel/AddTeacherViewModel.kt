package com.example.vidyatrack.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vidyatrack.data.repository.TeacherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddTeacherViewModel @Inject constructor(
    private val repository: TeacherRepository
) : ViewModel() {

    private val _uiState = mutableStateOf<AddTeacherUiState>(AddTeacherUiState.Idle)
    val uiState: State<AddTeacherUiState> = _uiState

    fun addTeacher(fullName: String, email: String, contactNumber: String, username: String, password: String) {
        _uiState.value = AddTeacherUiState.Loading
        viewModelScope.launch {
            val teacherMap = mapOf(
                "full_name" to fullName,
                "email" to email,
                "contact_number" to contactNumber,
                "username" to username,
                "password" to password
            )
            repository.addTeacher(teacherMap)
                .onSuccess {
                    _uiState.value = AddTeacherUiState.Success
                }
                .onFailure { error ->
                    _uiState.value = AddTeacherUiState.Error(error.message ?: "Unknown error")
                }
        }
    }
}

sealed class AddTeacherUiState {
    object Idle : AddTeacherUiState()
    object Loading : AddTeacherUiState()
    object Success : AddTeacherUiState()
    data class Error(val message: String) : AddTeacherUiState()
}
