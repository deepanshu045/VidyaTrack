package com.example.vidyatrack.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vidyatrack.data.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditStudentViewModel @Inject constructor(
    private val repository: StudentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val studentId: Int? = savedStateHandle.get<String>("studentId")?.toIntOrNull()

    private val _uiState = mutableStateOf<AddEditStudentUiState>(AddEditStudentUiState.Idle)
    val uiState: State<AddEditStudentUiState> = _uiState

    var fullName = mutableStateOf("")
    var dob = mutableStateOf("")
    var gender = mutableStateOf("Male")
    var parentName = mutableStateOf("")
    var parentContact = mutableStateOf("")
    var address = mutableStateOf("")
    var admissionDate = mutableStateOf("")
    var status = mutableStateOf("Active")

    init {
        if (studentId != null) {
            loadStudent(studentId)
        }
    }

    private fun loadStudent(id: Int) {
        _uiState.value = AddEditStudentUiState.Loading
        viewModelScope.launch {
            repository.getStudent(id)
                .onSuccess { student ->
                    fullName.value = student.fullName
                    dob.value = student.dob
                    gender.value = student.gender
                    parentName.value = student.parentName ?: ""
                    parentContact.value = student.parentContact ?: ""
                    address.value = student.address ?: ""
                    admissionDate.value = student.admissionDate ?: ""
                    status.value = student.status
                    _uiState.value = AddEditStudentUiState.Idle
                }
                .onFailure { error ->
                    _uiState.value = AddEditStudentUiState.Error(error.message ?: "Failed to load student")
                }
        }
    }

    fun saveStudent(onSuccess: () -> Unit) {
        val studentMap = mapOf(
            "full_name" to fullName.value,
            "dob" to dob.value,
            "gender" to gender.value,
            "parent_name" to parentName.value,
            "parent_contact" to parentContact.value,
            "address" to address.value,
            "admission_date" to admissionDate.value,
            "status" to status.value
        )

        _uiState.value = AddEditStudentUiState.Loading
        viewModelScope.launch {
            val result = if (studentId != null) {
                repository.updateStudent(studentId, studentMap)
            } else {
                repository.addStudent(studentMap)
            }

            result.onSuccess {
                _uiState.value = AddEditStudentUiState.Success
                onSuccess()
            }.onFailure { error ->
                _uiState.value = AddEditStudentUiState.Error(error.message ?: "Failed to save student")
            }
        }
    }
}

sealed class AddEditStudentUiState {
    object Idle : AddEditStudentUiState()
    object Loading : AddEditStudentUiState()
    object Success : AddEditStudentUiState()
    data class Error(val message: String) : AddEditStudentUiState()
}
