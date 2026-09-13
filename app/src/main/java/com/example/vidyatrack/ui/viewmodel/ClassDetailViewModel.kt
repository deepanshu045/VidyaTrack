package com.example.vidyatrack.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vidyatrack.data.remote.ClassResponse
import com.example.vidyatrack.data.remote.StudentResponse
import com.example.vidyatrack.data.remote.TeacherResponse
import com.example.vidyatrack.data.repository.ClassRepository
import com.example.vidyatrack.data.repository.StudentRepository
import com.example.vidyatrack.data.repository.TeacherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClassDetailViewModel @Inject constructor(
    private val classRepository: ClassRepository,
    private val studentRepository: StudentRepository,
    private val teacherRepository: TeacherRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val classId: String = checkNotNull(savedStateHandle["classId"])
    private val _uiState = mutableStateOf<ClassDetailUiState>(ClassDetailUiState.Loading)
    val uiState: State<ClassDetailUiState> = _uiState
    private val _availableStudents = mutableStateOf<List<StudentResponse>>(emptyList())
    val availableStudents: State<List<StudentResponse>> = _availableStudents
    private val _availableTeachers = mutableStateOf<List<TeacherResponse>>(emptyList())
    val availableTeachers: State<List<TeacherResponse>> = _availableTeachers

    init {
        loadClassDetail()
        loadAvailableResources()
    }

    fun loadClassDetail() {
        _uiState.value = ClassDetailUiState.Loading
        viewModelScope.launch {
            val id = classId.toInt()
            val classResult = classRepository.getClass(id)
            val studentsResult = classRepository.getClassStudents(id)
            val teachersResult = classRepository.getClassTeachers(id)
            if (classResult.isSuccess && studentsResult.isSuccess && teachersResult.isSuccess) {
                _uiState.value = ClassDetailUiState.Success(
                    classResult.getOrThrow(), studentsResult.getOrThrow(), teachersResult.getOrThrow()
                )
            } else {
                _uiState.value = ClassDetailUiState.Error("Failed to load class details")
            }
        }
    }

    private fun loadAvailableResources() {
        viewModelScope.launch {
            studentRepository.getStudents().onSuccess { _availableStudents.value = it }
            teacherRepository.getTeachers().onSuccess { _availableTeachers.value = it }
        }
    }

    fun assignStudent(studentId: Int) {
        viewModelScope.launch {
            classRepository.assignStudentToClass(classId.toInt(), studentId).onSuccess { loadClassDetail() }
        }
    }

    fun assignTeacher(teacherId: Int) {
        viewModelScope.launch {
            classRepository.assignTeacherToClass(classId.toInt(), teacherId).onSuccess { loadClassDetail() }
        }
    }

    fun updateClass(name: String, description: String, onDone: (Boolean) -> Unit) {
        if (name.isBlank()) {
            onDone(false)
            return
        }
        viewModelScope.launch {
            classRepository.updateClass(
                classId.toInt(),
                mapOf("name" to name.trim(), "description" to description.trim())
            ).onSuccess {
                loadClassDetail()
                onDone(true)
            }.onFailure {
                onDone(false)
            }
        }
    }

    fun deleteClass(onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            classRepository.deleteClass(classId.toInt()).onSuccess {
                onDone(true)
            }.onFailure {
                onDone(false)
            }
        }
    }
}

sealed class ClassDetailUiState {
    object Loading : ClassDetailUiState()
    data class Success(
        val classInfo: ClassResponse,
        val students: List<StudentResponse>,
        val teachers: List<TeacherResponse>
    ) : ClassDetailUiState()
    data class Error(val message: String) : ClassDetailUiState()
}
