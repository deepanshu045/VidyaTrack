package com.example.vidyatrack.data.repository

import com.example.vidyatrack.data.remote.ApiService
import com.example.vidyatrack.data.remote.StudentResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudentRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getStudents(): Result<List<StudentResponse>> {
        return try {
            val response = apiService.getStudents()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStudent(id: Int): Result<StudentResponse> {
        return try {
            val response = apiService.getStudent(id)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addStudent(student: Map<String, Any?>): Result<Any> {
        return try {
            val response = apiService.addStudent(student)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateStudent(id: Int, student: Map<String, Any?>): Result<Any> {
        return try {
            val response = apiService.updateStudent(id, student)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteStudent(id: Int): Result<Any> {
        return try {
            val response = apiService.deleteStudent(id)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
