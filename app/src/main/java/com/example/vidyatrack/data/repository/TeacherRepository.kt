package com.example.vidyatrack.data.repository

import com.example.vidyatrack.data.remote.ApiService
import com.example.vidyatrack.data.remote.TeacherResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeacherRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getTeachers(): Result<List<TeacherResponse>> {
        return try {
            val response = apiService.getTeachers()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addTeacher(teacher: Map<String, Any?>): Result<Any> {
        return try {
            val response = apiService.addTeacher(teacher)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTeacher(id: Int, teacher: Map<String, Any?>): Result<Any> {
        return try {
            val response = apiService.updateTeacher(id, teacher)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
