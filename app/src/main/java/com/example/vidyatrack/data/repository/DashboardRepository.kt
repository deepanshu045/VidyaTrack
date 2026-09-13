package com.example.vidyatrack.data.repository

import com.example.vidyatrack.data.remote.AdminStatsResponse
import com.example.vidyatrack.data.remote.ApiService
import com.example.vidyatrack.data.remote.TeacherSummaryResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getAdminStats(): Result<AdminStatsResponse> {
        return try {
            val response = apiService.getAdminStats()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTeacherSummary(): Result<TeacherSummaryResponse> {
        return try {
            val response = apiService.getTeacherSummary()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
