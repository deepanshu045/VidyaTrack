package com.example.vidyatrack.data.repository

import com.example.vidyatrack.data.remote.ApiService
import com.example.vidyatrack.data.remote.AttendanceHistoryItem
import com.example.vidyatrack.data.remote.ClassAttendanceResponse
import com.example.vidyatrack.data.remote.StudentResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getClassStudents(classId: Int): Result<List<StudentResponse>> = try {
        Result.success(apiService.getClassStudents(classId))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getTodayAttendance(classId: Int, date: String): Result<ClassAttendanceResponse> = try {
        Result.success(apiService.getClassAttendance(classId, date))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getHistory(classId: Int): Result<List<AttendanceHistoryItem>> = try {
        Result.success(apiService.getClassAttendanceHistory(classId))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun saveAttendance(
        classId: Int,
        date: String,
        records: List<Map<String, Any>>
    ): Result<Any> = try {
        Result.success(
            apiService.markAttendance(
                mapOf(
                    "class_id" to classId,
                    "date" to date,
                    "records" to records
                )
            )
        )
    } catch (e: Exception) {
        Result.failure(e)
    }
}
