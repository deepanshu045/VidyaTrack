package com.example.vidyatrack.data.repository

import com.example.vidyatrack.data.remote.ApiService
import com.example.vidyatrack.data.remote.ClassResponse
import com.example.vidyatrack.data.remote.StudentResponse
import com.example.vidyatrack.data.remote.TeacherResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClassRepository @Inject constructor(private val apiService: ApiService) {
    suspend fun getClasses(): Result<List<ClassResponse>> = try { Result.success(apiService.getClasses()) } catch (e: Exception) { Result.failure(e) }
    suspend fun addClass(classData: Map<String, String>): Result<Any> = try { Result.success(apiService.addClass(classData)) } catch (e: Exception) { Result.failure(e) }
    suspend fun getClass(id: Int): Result<ClassResponse> = try { Result.success(apiService.getClass(id)) } catch (e: Exception) { Result.failure(e) }
    suspend fun updateClass(id: Int, classData: Map<String, String>): Result<Any> = try { Result.success(apiService.updateClass(id, classData)) } catch (e: Exception) { Result.failure(e) }
    suspend fun deleteClass(id: Int): Result<Any> = try { Result.success(apiService.deleteClass(id)) } catch (e: Exception) { Result.failure(e) }
    suspend fun getClassStudents(id: Int): Result<List<StudentResponse>> = try { Result.success(apiService.getClassStudents(id)) } catch (e: Exception) { Result.failure(e) }
    suspend fun getClassTeachers(id: Int): Result<List<TeacherResponse>> = try { Result.success(apiService.getClassTeachers(id)) } catch (e: Exception) { Result.failure(e) }
    suspend fun assignStudentToClass(classId: Int, studentId: Int): Result<Any> = try { Result.success(apiService.assignStudentToClass(classId, studentId)) } catch (e: Exception) { Result.failure(e) }
    suspend fun assignTeacherToClass(classId: Int, teacherId: Int): Result<Any> = try { Result.success(apiService.assignTeacherToClass(classId, teacherId)) } catch (e: Exception) { Result.failure(e) }
}
