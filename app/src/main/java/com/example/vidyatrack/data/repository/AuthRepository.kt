package com.example.vidyatrack.data.repository

import com.example.vidyatrack.data.SessionManager
import com.example.vidyatrack.data.remote.ApiService
import com.example.vidyatrack.data.remote.LoginResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    suspend fun login(credentials: Map<String, String>): Result<LoginResponse> {
        return try {
            val response = apiService.login(credentials)
            sessionManager.saveAuthToken(response.token)
            sessionManager.saveUserRole(response.role)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        sessionManager.clearData()
    }

    fun getUserRole(): String? = sessionManager.fetchUserRole()
    fun getAuthToken(): String? = sessionManager.fetchAuthToken()
}
