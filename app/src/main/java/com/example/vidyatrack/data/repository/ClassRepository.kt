package com.example.vidyatrack.data.repository

import com.example.vidyatrack.data.remote.ApiService
import com.example.vidyatrack.data.remote.ClassResponse
import com.example.vidyatrack.data.remote.StudentResponse
import com.example.vidyatrack.data.remote.TeacherResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClassRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getClasses(): Result<List<ClassResponse>> {
        return try {
            val response = apiService.getClasses()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addClass(classData: Map<String, String>): Result<Any> {
        return try {
            val response = apiService.addClass(classData)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getClass(id: Int): Result<ClassResponse> {
        return try {
            val response = apiService.getClass(id)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getClassStudents(id: Int): Result<List<StudentResponse>> {
        return try {
            val response = apiService.getClassStudents(id)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getClassTeachers(id: Int): Result<List<TeacherResponse>> {
        return try {
            val response = apiService.getClassTeachers(id)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun assignStudentToClass(classId: Int, studentId: Int): Result<Any> {
        return try {
            val response = apiService.assignStudentToClass(classId, studentId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun assignTeacherToClass(classId: Int, teacherId: Int): Result<Any> {
        return try {
            val response = apiService.assignTeacherToClass(classId, teacherId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
