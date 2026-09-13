package com.example.vidyatrack.data.remote

import retrofit2.http.*

interface ApiService {
    @POST("api/auth/login") suspend fun login(@Body body: Map<String, String>): LoginResponse
    @GET("api/students") suspend fun getStudents(): List<StudentResponse>
    @POST("api/students") suspend fun addStudent(@Body student: Map<String, Any?>): Any
    @GET("api/students/{id}") suspend fun getStudent(@Path("id") id: Int): StudentResponse
    @PUT("api/students/{id}") suspend fun updateStudent(@Path("id") id: Int, @Body student: Map<String, Any?>): Any
    @DELETE("api/students/{id}") suspend fun deleteStudent(@Path("id") id: Int): Any
    @GET("api/teachers") suspend fun getTeachers(): List<TeacherResponse>
    @POST("api/teachers") suspend fun addTeacher(@Body teacher: Map<String, Any?>): Any
    @PUT("api/teachers/{id}") suspend fun updateTeacher(@Path("id") id: Int, @Body teacher: Map<String, Any?>): Any
    @GET("api/classes") suspend fun getClasses(): List<ClassResponse>
    @POST("api/classes") suspend fun addClass(@Body classData: Map<String, String>): Any
    @GET("api/classes/{id}") suspend fun getClass(@Path("id") id: Int): ClassResponse
    @PUT("api/classes/{id}") suspend fun updateClass(@Path("id") id: Int, @Body classData: Map<String, String>): Any
    @DELETE("api/classes/{id}") suspend fun deleteClass(@Path("id") id: Int): Any
    @GET("api/classes/{id}/students") suspend fun getClassStudents(@Path("id") id: Int): List<StudentResponse>
    @GET("api/classes/{id}/teachers") suspend fun getClassTeachers(@Path("id") id: Int): List<TeacherResponse>
    @POST("api/classes/{class_id}/students/{student_id}") suspend fun assignStudentToClass(@Path("class_id") classId: Int, @Path("student_id") studentId: Int): Any
    @POST("api/classes/{class_id}/teachers/{teacher_id}") suspend fun assignTeacherToClass(@Path("class_id") classId: Int, @Path("teacher_id") teacherId: Int): Any
    @GET("api/health") suspend fun healthCheck(): Any
    @GET("api/dashboard/admin/stats") suspend fun getAdminStats(): AdminStatsResponse
    @GET("api/dashboard/teacher/summary") suspend fun getTeacherSummary(): TeacherSummaryResponse
    @POST("api/attendance") suspend fun markAttendance(@Body data: Map<String, Any?>): Any
    @GET("api/attendance/class/{class_id}/date/{date}") suspend fun getClassAttendance(@Path("class_id") classId: Int, @Path("date") date: String): ClassAttendanceResponse
    @GET("api/attendance/class/{class_id}/history") suspend fun getClassAttendanceHistory(@Path("class_id") classId: Int): List<AttendanceHistoryItem>
}

data class ClassAttendanceResponse(val session_id: Int?, val records: List<AttendanceRecordResponse>)
data class AttendanceRecordResponse(val student_id: Int, val student_name: String, val status: String)
data class AttendanceHistoryItem(val id: Int, val date: String, val present_count: Int, val total_count: Int)
