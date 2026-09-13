package com.example.vidyatrack.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vidyatrack.data.remote.AttendanceHistoryItem
import com.example.vidyatrack.data.remote.AttendanceRecordResponse
import com.example.vidyatrack.data.remote.StudentResponse
import com.example.vidyatrack.data.repository.AttendanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val repository: AttendanceRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val classId: Int = checkNotNull(savedStateHandle["classId"])
    private val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private val _uiState = mutableStateOf<AttendanceUiState>(AttendanceUiState.Loading)
    val uiState: State<AttendanceUiState> = _uiState

    private val _statuses = mutableStateOf<Map<Int, String>>(emptyMap())
    val statuses: State<Map<Int, String>> = _statuses

    private val _history = mutableStateOf<List<AttendanceHistoryItem>>(emptyList())
    val history: State<List<AttendanceHistoryItem>> = _history

    private val _saving = mutableStateOf(false)
    val saving: State<Boolean> = _saving

    private val _message = mutableStateOf<String?>(null)
    val message: State<String?> = _message

    init {
        load()
    }

    fun load() {
        _uiState.value = AttendanceUiState.Loading
        viewModelScope.launch {
            val studentsResult = repository.getClassStudents(classId)
            val todayResult = repository.getTodayAttendance(classId, date)
            val historyResult = repository.getHistory(classId)

            if (studentsResult.isSuccess && todayResult.isSuccess) {
                val students = studentsResult.getOrThrow()
                val existing = todayResult.getOrThrow().records.associate { it.student_id to it.status }
                _statuses.value = students.associate { it.id to (existing[it.id] ?: "Present") }
                if (historyResult.isSuccess) _history.value = historyResult.getOrThrow()
                _uiState.value = AttendanceUiState.Success(students, date)
            } else {
                _uiState.value = AttendanceUiState.Error("Failed to load class attendance")
            }
        }
    }

    fun setStatus(studentId: Int, status: String) {
        if (status == "Present" || status == "Absent") {
            _statuses.value = _statuses.value + (studentId to status)
        }
    }

    fun save() {
        val current = _uiState.value
        if (current !is AttendanceUiState.Success) return
        _saving.value = true
        _message.value = null
        viewModelScope.launch {
            val records = current.students.map {
                mapOf<String, Any>(
                    "student_id" to it.id,
                    "status" to (_statuses.value[it.id] ?: "Present")
                )
            }
            repository.saveAttendance(classId, current.date, records)
                .onSuccess {
                    _message.value = "Attendance saved successfully"
                    load()
                }
                .onFailure { _message.value = it.message ?: "Failed to save attendance" }
            _saving.value = false
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}

sealed class AttendanceUiState {
    object Loading : AttendanceUiState()
    data class Success(val students: List<StudentResponse>, val date: String) : AttendanceUiState()
    data class Error(val message: String) : AttendanceUiState()
}
